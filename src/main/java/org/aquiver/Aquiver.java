/*
 * MIT License
 *
 * Copyright (c) 2019 1619kHz
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.aquiver;

import org.apache.commons.lang3.Validate;
import org.apex.Apex;
import org.apex.ApexContext;
import org.apex.Environment;
import org.aquiver.handler.ErrorHandler;
import org.aquiver.handler.ErrorHandlerResolver;
import org.aquiver.mvc.annotation.HttpMethod;
import org.aquiver.mvc.interceptor.Interceptor;
import org.aquiver.mvc.router.RestfulRouter;
import org.aquiver.mvc.router.session.SessionManager;
import org.aquiver.server.NettyServer;
import org.aquiver.server.Server;
import org.aquiver.server.banner.BannerFont;
import org.aquiver.websocket.WebSocketChannel;
import org.aquiver.websocket.WebSocketResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

import static io.netty.util.internal.PlatformDependent.isWindows;
import static java.util.Objects.requireNonNull;
import static org.aquiver.server.Const.BANNER_TEXT;
import static org.aquiver.server.Const.PATH_APP_BANNER_FONT;
import static org.aquiver.server.Const.PATH_APP_BANNER_TEXT;
import static org.aquiver.server.Const.PATH_CONFIG_PROPERTIES;
import static org.aquiver.server.Const.PATH_SERVER_CONTENT_COMPRESSOR;
import static org.aquiver.server.Const.PATH_SERVER_CORS;
import static org.aquiver.server.Const.PATH_SERVER_PORT;
import static org.aquiver.server.Const.PATH_SERVER_SESSION_ENABLE;
import static org.aquiver.server.Const.PATH_SERVER_SESSION_KEY;
import static org.aquiver.server.Const.PATH_SERVER_SESSION_TIMEOUT;
import static org.aquiver.server.Const.PATH_SERVER_TEMPLATES_FOLDER;
import static org.aquiver.server.Const.PATH_SERVER_VIEW_SUFFIX;
import static org.aquiver.server.Const.SERVER_CONTENT_COMPRESSOR;
import static org.aquiver.server.Const.SERVER_CORS;
import static org.aquiver.server.Const.SERVER_SESSION_ENABLE;
import static org.aquiver.server.Const.SERVER_SESSION_KEY;
import static org.aquiver.server.Const.SERVER_SESSION_TIMEOUT;
import static org.aquiver.server.Const.SERVER_TEMPLATES_FOLDER;
import static org.aquiver.server.Const.SERVER_THREAD_NAME;

/**
 * Aquiver is apex context, can use method configure
 * <p>
 * A simple startup example，Bind port after instantiated by {@linkplain #of()}
 * <pre>{@code
 *  public class Application {
 *    public static void main(String[] args) {
 *      Aquiver.of().bind(8081).start(Application.class, args);
 *    }
 *  }
 * }</pre>
 * <p>
 * Or you can use {@linkplain #run(Class, String...)} to start directly, Usage example:
 * <pre>{@code
 *  public class Application {
 *    public static void main(String[] args) {
 *      Aquiver.run(Application.class, args);
 *    }
 *  }
 * }
 * </pre>
 *
 * @author WangYi
 * @since 2019/6/5
 */
public class Aquiver {
  private static final Logger log = LoggerFactory.getLogger(Aquiver.class);

  // Components needed to start the service.
  private final Server nettyServer = new NettyServer();
  private final Set<String> packages = new LinkedHashSet<>();
  private final List<Class<?>> eventPool = new LinkedList<>();
  private final CountDownLatch countDownLatch = new CountDownLatch(1);
  private final SessionManager sessionManager = new SessionManager();
  private Environment environment = Apex.of().environment();
  private String bootConfName = PATH_CONFIG_PROPERTIES;
  private boolean started = false;

  // Some content that may be needed.
  private int port;
  private Class<?> bootCls;
  private String bannerText;
  private String bannerFont;
  private ExecutorService reusableExecutor;
  private String sessionKey;
  private Integer sessionTimeout;
  private String[] mainArgs;

  // Thread pool setting param
  private int corePoolSize = 5;
  private int maximumPoolSize = 200;
  private int keepAliveTime = 0;

  private String viewSuffix;
  private String templateFolder;

  // Components needed for dependency injection
  private final Apex apex = Apex.of();
  private final ApexContext apexContext = ApexContext.instance();

  // A series of components used
  private final RestfulRouter restfulRouter = apexContext.addBean(RestfulRouter.class);
  private final WebSocketResolver webSocketResolver = apexContext.addBean(WebSocketResolver.class);
  private final ErrorHandlerResolver errorHandlerResolver = apexContext.addBean(ErrorHandlerResolver.class);

  private static final List<Interceptor> interceptors = new ArrayList<>();

  private Aquiver() {}

  /**
   * Return Aquiver instants
   *
   * @return Aquiver instants
   */
  public static Aquiver of() {
    return AquiverHolder.instance();
  }

  /**
   * Start service with default configuration
   *
   * @param bootClass started main class
   * @param args      main method param array
   * @return Aquiver
   */
  public static Aquiver run(Class<?> bootClass, String... args) {
    final Aquiver aquiver = of();
    aquiver.start(bootClass, args);
    return aquiver;
  }

  /**
   * Get interceptor list
   *
   * @return interceptor list
   */
  public static List<Interceptor> interceptors() {
    return interceptors;
  }

  public static String getCurrentClassPath() {
    URL url = Aquiver.class.getResource("/");
    String path;
    if (Objects.isNull(url)) {
      File f = new File(Aquiver.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      path = f.getPath();
    } else {
      path = url.getPath();
    }

    String decode = "/";
    try {
      if (isWindows()) {
        decode = decode(path.replaceFirst("^/(.:/)", "$1"));
      } else {
        decode = decode(path);
      }
    } catch (UnsupportedEncodingException e) {
      log.info("Un supported encoding", e);
    }
    return decode;
  }

  public static String decode(String path) throws UnsupportedEncodingException {
    return java.net.URLDecoder.decode(path, StandardCharsets.UTF_8.name());
  }

  /**
   * Set the listening port number.
   */
  public Aquiver bind(int port) {
    Validate.isTrue(port > 0 && port <= 65533, "Port number must be available");
    Validate.isTrue(this.port >= 0, "port was already set to %s", this.port);
    this.port = port;
    this.environment.add(PATH_SERVER_PORT, port);
    return this;
  }

  /**
   * Set the print banner text
   */
  public Aquiver bannerText(String bannerText) {
    Validate.notNull(this.bannerText, "bannerText was already set to %s", this.bannerText);
    this.bannerText = requireNonNull(bannerText);
    this.environment.add(PATH_APP_BANNER_TEXT, bannerText);
    return this;
  }

  /**
   * Set the print banner font
   */
  public Aquiver bannerFont(String bannerFont) {
    Validate.notNull(this.bannerFont, "bannerFont was already set to %s", this.bannerFont);
    this.bannerFont = requireNonNull(bannerFont);
    this.environment.add(PATH_APP_BANNER_FONT, bannerFont);
    return this;
  }

  /**
   * Set the print banner name
   */
  public Aquiver bootConfName(String bootConfName) {
    Validate.notNull(this.bootConfName, "bootConfName was already set to %s", this.bootConfName);
    this.bootConfName = requireNonNull(bootConfName);
    this.environment.add(PATH_CONFIG_PROPERTIES, bootConfName);
    return this;
  }

  /**
   * Set render view suffix
   *
   * @param viewSuffix view suffix
   */
  public Aquiver viewSuffix(String viewSuffix) {
    Validate.notNull(this.viewSuffix, "viewSuffix was already set to %s", this.viewSuffix);
    this.viewSuffix = requireNonNull(viewSuffix);
    this.environment.add(PATH_SERVER_VIEW_SUFFIX, viewSuffix);
    return this;
  }

  /**
   * Set template folder
   *
   * @param folder template folder
   */
  public Aquiver templateFolder(String folder) {
    Validate.notNull(this.templateFolder, "templateFolder was already set to %s", this.templateFolder);
    this.templateFolder = requireNonNull(folder);
    this.environment.add(PATH_SERVER_TEMPLATES_FOLDER, folder);
    return this;
  }

  /**
   * Get template folder
   *
   * @return template folder
   */
  public String templateFolder() {
    if (templateFolder == null) {
      return this.environment.getString(PATH_SERVER_TEMPLATES_FOLDER, SERVER_TEMPLATES_FOLDER);
    }
    return templateFolder;
  }

  /**
   * Set server session key
   *
   * @param sessionKey session key
   * @return this
   */
  public Aquiver sessionKey(String sessionKey) {
    Validate.notNull(this.sessionKey, "sessionKey was already set to %s", this.sessionKey);
    this.sessionKey = requireNonNull(sessionKey);
    this.environment.add(PATH_SERVER_SESSION_KEY, sessionKey);
    return this;
  }

  /**
   * Get server session key
   *
   * @return session key
   */
  public String sessionKey() {
    if (sessionKey == null) {
      return this.environment.getString(PATH_SERVER_SESSION_KEY, SERVER_SESSION_KEY);
    }
    return sessionKey;
  }

  /**
   * Session time out
   *
   * @param sessionTimeout time out
   * @return this
   */
  public Aquiver sessionTimeout(Integer sessionTimeout) {
    Validate.notNull(this.sessionTimeout, "sessionTimeout was already set to %s", this.sessionTimeout);
    this.sessionTimeout = requireNonNull(sessionTimeout);
    this.environment.add(PATH_SERVER_SESSION_TIMEOUT, sessionTimeout);
    return this;
  }

  /**
   * Get session time out
   *
   * @return session time out
   */
  public Integer sessionTimeout() {
    if (sessionTimeout == null) {
      return this.environment.getInt(PATH_SERVER_SESSION_TIMEOUT, SERVER_SESSION_TIMEOUT);
    }
    return sessionTimeout;
  }

  /**
   * Set session open state, the default is open
   *
   * @param enable Whether to open the session
   * @return this
   */
  public Aquiver sessionEnable(boolean enable) {
    this.environment.add(PATH_SERVER_SESSION_ENABLE, enable);
    return this;
  }

  /**
   * Get session enable status
   *
   * @return session enable status
   */
  public boolean sessionEnable() {
    return this.environment.getBoolean(PATH_SERVER_SESSION_ENABLE, SERVER_SESSION_ENABLE);
  }

  /**
   * Returns the listening port number
   *
   * @return port number
   */
  public int port() {
    Validate.isTrue(this.port > 0 && port <= 65533, "Port number must be available");
    return port;
  }

  /**
   * Get a unified environment object
   *
   * @return environment object
   */
  public Environment environment() {
    return environment;
  }

  /**
   * Get Session Manager
   *
   * @return Session manager
   */
  public SessionManager sessionManager() {
    return sessionManager;
  }

  /**
   * Set a environment object
   *
   * @param environment environment object
   */
  public void environment(Environment environment) {
    this.environment = environment;
  }

  /**
   * Get the startup class that calls the start or run method
   *
   * @return started class
   */
  public Class<?> bootCls() {
    return requireNonNull(bootCls);
  }

  /**
   * Get started class name
   *
   * @return class name
   */
  public String bootClsName() {
    return bootCls.getSimpleName();
  }

  /**
   * Get main method args
   *
   * @return main method args
   */
  public String[] mainArgs() {
    return mainArgs;
  }

  /**
   * Get banner text
   *
   * @return banner text
   */
  public String bannerText() {
    if (bannerText == null) {
      return this.environment.getString(PATH_APP_BANNER_TEXT, BANNER_TEXT);
    }
    return bannerText;
  }

  /**
   * Get banner font
   *
   * @return banner font
   */
  public String bannerFont() {
    if (Objects.isNull(this.bannerFont)) {
      return this.environment.getString(PATH_APP_BANNER_FONT, BannerFont.FONT_DEFAULT);
    }
    return bannerFont;
  }

  /**
   * Get start up server config properties name
   *
   * @return start up server config properties name
   */
  public String bootConfName() {
    return requireNonNull(bootConfName);
  }

  /**
   * Get scan path list
   *
   * @return scan path list
   */
  public Set<String> scanPaths() {
    return requireNonNull(packages);
  }

  /**
   * Get event list
   *
   * @return event list
   */
  public List<Class<?>> eventPool() {
    return requireNonNull(this.eventPool);
  }

  /**
   * the number of threads to keep in the pool, even
   * if they are idle, unless {@code allowCoreThreadTimeOut} is set
   *
   * @param corePoolSize setting thread pool size
   * @return Aquiver
   */
  public Aquiver corePoolSize(int corePoolSize) {
    this.corePoolSize = corePoolSize;
    return this;
  }

  /**
   * the maximum number of threads to allow in the pool
   *
   * @param maximumPoolSize thread pool maximum pool size
   * @return Aquiver
   */
  public Aquiver maximumPoolSize(int maximumPoolSize) {
    this.maximumPoolSize = maximumPoolSize;
    return this;
  }

  /**
   * when the number of threads is greater than
   * the core, this is the maximum time that excess idle threads
   * will wait for new tasks before terminating.
   *
   * @param keepAliveTime keep live time
   * @return Aquiver
   */
  public Aquiver keepAliveTime(int keepAliveTime) {
    this.keepAliveTime = keepAliveTime;
    return this;
  }

  /**
   * Get render view suffix
   *
   * @return view suffix
   */
  public String viewSuffix() {
    return viewSuffix;
  }

  /**
   * Get CORS on status
   *
   * @return cors status
   */
  public boolean cors() {
    return this.environment().getBoolean(PATH_SERVER_CORS, SERVER_CORS);
  }

  /**
   * Get Gzip on status
   *
   * @return Gzip status
   */
  public boolean gzip() {
    return this.environment().getBoolean(PATH_SERVER_CONTENT_COMPRESSOR, SERVER_CONTENT_COMPRESSOR);
  }

  /**
   * Register websocket route
   *
   * @param path             Websocket route path
   * @param webSocketChannel WebSocket abstract interface
   * @return this
   */
  public Aquiver websocket(String path, WebSocketChannel webSocketChannel) {
    Validate.notNull(path, "WebSocket url path can't be null");
    Validate.notNull(webSocketChannel, "WebSocketChannel can't be null");
    this.webSocketResolver.registerWebSocket(path, webSocketChannel);
    return this;
  }

  /**
   * Register get route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver get(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.GET);
    return this;
  }

  /**
   * Register post route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver post(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.POST);
    return this;
  }

  /**
   * Register head route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver head(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.HEAD);
    return this;
  }

  /**
   * Register put route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver put(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.PUT);
    return this;
  }

  /**
   * Register patch route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver patch(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.PATCH);
    return this;
  }

  /**
   * Register delete route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver delete(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.DELETE);
    return this;
  }

  /**
   * Register options route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver options(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.OPTIONS);
    return this;
  }

  /**
   * Register trace route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver trace(String path, RequestHandler requestHandler) {
    this.route(path, requestHandler, HttpMethod.TRACE);
    return this;
  }

  /**
   * Register route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver route(String path, RequestHandler requestHandler, HttpMethod httpMethod) {
    Validate.notBlank(path, "Route path can't be null");
    Validate.notNull(httpMethod, "Need to specify of http request method registered");
    Validate.notNull(requestHandler, "RequestHandler object can't be null");
    this.restfulRouter.registerRoute(path, requestHandler, httpMethod);
    return this;
  }

  /**
   * Register exception advice
   *
   * @param throwableCls exception
   * @param errorHandler exception handler
   * @return this
   */
  public Aquiver errorHandler(Class<? extends Throwable> throwableCls, ErrorHandler errorHandler) {
    this.errorHandlerResolver.registerErrorHandler(throwableCls, errorHandler);
    return this;
  }

  /**
   * Register interceptor
   *
   * @param interceptor http interceptor
   * @return this
   */
  public Aquiver interceptor(Interceptor interceptor) {
    Aquiver.interceptors.add(interceptor);
    return this;
  }

  /**
   * Get apex instance
   * @return apex instance
   */
  public Apex apex() {
    return apex;
  }

  /**
   * Get apexContext instance
   * @return ApexContext instance
   */
  public ApexContext apexContext() {
    return apexContext;
  }

  /**
   * Open an http service, which is implemented by netty by default
   *
   * @param bootClass start up class
   * @param args      main method param array
   */
  public void start(Class<?> bootClass, String[] args) {
    try {
      this.mainArgs = args;
      this.initReusableThreadPool();
    } catch (Exception e) {
      log.error("An exception occurred while loading the configuration", e);
    }
    this.reusableExecutor.execute(() -> {
      try {
        this.bootCls = bootClass;
        this.nettyServer.start(this);
        this.countDownLatch.countDown();
        this.nettyServer.join();
      } catch (BindException e) {
        log.error("Bind port is exception:", e);
      } catch (Exception e) {
        log.error("An exception occurred while the service started", e);
      } finally {
        log.info("SingleExecutor graceful shutdown");
        this.reusableExecutor.shutdown();
      }
    });
    this.started = true;
  }

  /**
   * init default param reusable executor
   */
  private void initReusableThreadPool() {
    this.initReusableThreadPool(corePoolSize, maximumPoolSize, keepAliveTime);
  }

  /**
   * Instantiation reusable executor based on parameters
   *
   * @param corePoolSize    the number of threads to keep in the pool, even
   *                        if they are idle, unless {@code allowCoreThreadTimeOut} is set
   * @param maximumPoolSize the maximum number of threads to allow in the pool
   * @param keepAliveTime   when the number of threads is greater than
   *                        the core, this is the maximum time that excess idle threads
   *                        will wait for new tasks before terminating.
   */
  private void initReusableThreadPool(int corePoolSize, int maximumPoolSize, int keepAliveTime) {
    final AquiverThreadFactory aquiverThreadFactory = new AquiverThreadFactory(SERVER_THREAD_NAME);
    final LinkedBlockingQueue<Runnable> runnableQueue = new LinkedBlockingQueue<>(16);
    final ThreadPoolExecutor.AbortPolicy abortPolicy = new ThreadPoolExecutor.AbortPolicy();
    this.reusableExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime,
            TimeUnit.MILLISECONDS, runnableQueue, aquiverThreadFactory, abortPolicy);
  }

  /**
   * Await web server started
   *
   * @return return Aquiver instance
   */
  public Aquiver await() {
    if (!this.started) {
      throw new IllegalStateException("Server hasn't been started. " +
              "Call start() before calling this method.");
    }
    try {
      this.countDownLatch.await();
    } catch (Exception e) {
      log.error("Server start await error", e);
      Thread.currentThread().interrupt();
    }
    return this;
  }

  /**
   * Stop web server
   */
  public void stop() {
    this.nettyServer.stop();
  }

  /**
   * Aquiver thread factory
   */
  static class AquiverThreadFactory implements ThreadFactory {

    private final String prefix;
    private final LongAdder threadNumber = new LongAdder();

    public AquiverThreadFactory(String prefix) {
      this.threadNumber.add(1);
      this.prefix = prefix;
    }

    /**
     * Constructs a new {@code Thread}.  Implementations may also initialize
     * priority, name, daemon status, {@code ThreadGroup}, etc.
     *
     * @param runnable a runnable to be executed by new thread instance
     * @return constructed thread, or {@code null} if the request to
     * create a thread is rejected
     */
    @Override
    public Thread newThread(Runnable runnable) {
      return new Thread(runnable, prefix + "thread-" + threadNumber.intValue());
    }
  }

  /**
   * Aquiver object holder
   */
  private static class AquiverHolder {
    private static final Aquiver instance = new Aquiver();

    /**
     * Get Singleton aquiver object
     */
    public static Aquiver instance() {
      return instance;
    }
  }
}
