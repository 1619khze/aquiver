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

import org.aquiver.function.AdviceManager;
import org.aquiver.mvc.annotation.HttpMethod;
import org.aquiver.mvc.resolver.ParamResolverManager;
import org.aquiver.mvc.route.RouteManager;
import org.aquiver.mvc.route.session.SessionManager;
import org.aquiver.server.NettyServer;
import org.aquiver.server.Server;
import org.aquiver.server.banner.BannerFont;
import org.aquiver.utils.PropertyUtils;
import org.aquiver.websocket.WebSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.net.BindException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;

import static java.util.Objects.requireNonNull;
import static org.aquiver.Const.*;

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
public final class Aquiver {
  private static final Logger log = LoggerFactory.getLogger(Aquiver.class);

  /** Components needed to start the service. */
  private final Server nettyServer = new NettyServer();
  private final Set<String> packages = new LinkedHashSet<>();
  private final List<Class<?>> eventPool = new LinkedList<>();
  private final CountDownLatch countDownLatch = new CountDownLatch(1);
  private final SessionManager sessionManager = new SessionManager();
  private Environment environment = new Environment();
  private String bootConfName = PATH_CONFIG_PROPERTIES;
  private String envName = "default";

  /** Parameters needed to read the configuration file. */
  private boolean envConfig = false;
  private boolean masterConfig = false;
  private boolean started = false;

  /** Classgraph related configuration. */
  private boolean verbose = false;
  private boolean realtimeLogging = false;

  /** Some content that may be needed. */
  private int port;
  private Class<?> bootCls;
  private String bannerText;
  private String bannerFont;
  private ExecutorService reusableExecutor;
  private String sessionKey;
  private Integer sessionTimeout;

  /** Thread pool setting param */
  private int corePoolSize = 5;
  private int maximumPoolSize = 200;
  private int keepAliveTime = 0;

  private String viewSuffix;

  /** Web components that need to be initialized */
  private final AdviceManager adviceManager = new AdviceManager();
  private final RouteManager routeManager;
  private final ParamResolverManager resolverManager;

  public RouteManager routeManager() {
    return routeManager;
  }

  public AdviceManager adviceManager() {
    return adviceManager;
  }

  public ParamResolverManager resolverManager() {
    return resolverManager;
  }

  private Aquiver() {
    this.routeManager = new RouteManager();
    this.resolverManager = new ParamResolverManager();
    try {
      this.resolverManager.initialize();
      this.routeManager.setResolverManager(resolverManager);
    } catch (Exception e) {
      log.error("An exception occurred when Aquiver was instantiated", e);
    }
  }

  /** Ensures that the argument expression is true. */
  static void requireArgument(boolean expression) {
    if (!expression) {
      throw new IllegalArgumentException();
    }
  }

  /** Ensures that the argument expression is true */
  static void requireArgument(boolean expression, String template, Object... args) {
    if (!expression) {
      throw new IllegalArgumentException(String.format(template, args));
    }
  }

  /** Ensures that the state expression is true. */
  static void requireState(boolean expression) {
    if (!expression) {
      throw new IllegalStateException();
    }
  }

  /** Ensures that the state expression is true. */
  static void requireState(boolean expression, String template, Object... args) {
    if (!expression) {
      throw new IllegalStateException(String.format(template, args));
    }
  }

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

  /** Set the listening port number. */
  public Aquiver bind(int port) {
    requireArgument(port > 0 && port <= 65533, "Port number must be available");
    requireArgument(this.port >= 0, "port was already set to %s", this.port);
    this.port = port;
    this.environment.add(PATH_SERVER_PORT, port);
    return this;
  }

  /** Set the print banner text */
  public Aquiver bannerText(String bannerText) {
    requireState(this.bannerText == null, "bannerText was already set to %s", this.bannerText);
    this.bannerText = requireNonNull(bannerText);
    this.environment.add(PATH_APP_BANNER_TEXT, bannerText);
    return this;
  }

  /** Set the print banner font */
  public Aquiver bannerFont(String bannerFont) {
    requireState(this.bannerFont == null, "bannerFont was already set to %s", this.bannerFont);
    this.bannerFont = requireNonNull(bannerFont);
    this.environment.add(PATH_APP_BANNER_FONT, bannerFont);
    return this;
  }

  /** Set the print banner name */
  public Aquiver bootConfName(String bootConfName) {
    requireState(this.bootConfName == null, "bootConfName was already set to %s", this.bootConfName);
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
    requireState(this.viewSuffix == null, "viewSuffix was already set to %s", this.viewSuffix);
    this.viewSuffix = requireNonNull(viewSuffix);
    this.environment.add(PATH_SERVER_VIEW_SUFFIX, viewSuffix);
    return this;
  }

  /**
   * Set server session key
   *
   * @param sessionKey session key
   * @return this
   */
  public Aquiver sessionKey(String sessionKey) {
    requireArgument(this.sessionKey == null, "sessionKey was already set to %s", this.sessionKey);
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
    requireArgument(this.sessionTimeout == null, "sessionTimeout was already set to %s", this.sessionTimeout);
    this.sessionTimeout = requireNonNull(sessionTimeout);
    this.environment.add(PATH_SERVER_SESSION_TIMEOUT, sessionTimeout);
    return this;
  }

  /**
   * Get session time out
   *
   * @return
   */
  public Integer sessionTimeout() {
    if (sessionTimeout == null) {
      return this.environment.getInteger(PATH_SERVER_SESSION_TIMEOUT, SERVER_SESSION_TIMEOUT);
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
    requireArgument(this.port > 0 && port <= 65533, "Port number must be available");
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
   * Get environment config load status
   *
   * @return environment config load status
   */
  public boolean envConfig() {
    return envConfig;
  }

  /**
   * Get master config properties or yaml load status
   *
   * @return master config properties or yaml load status
   */
  public boolean masterConfig() {
    return masterConfig;
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
   * Get environment name
   *
   * @return environment name
   */
  public String envName() {
    return requireNonNull(envName);
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
   * Set Whether to start the detailed scan log of classgraph
   *
   * @param verbose Whether to enable detailed scan log
   * @return Aquiver
   */
  public Aquiver verbose(boolean verbose) {
    this.verbose = verbose;
    this.environment.add(PATH_SCANNER_VERBOSE, verbose);
    return this;
  }

  /**
   * Get Whether to start the detailed scan log of classgraph
   *
   * @return Whether to enable detailed scan log
   */
  public boolean verbose() {
    return requireNonNull(this.environment.getBoolean(PATH_SCANNER_VERBOSE, verbose));
  }

  /**
   * Set Whether to enable real-time recording of classgraph
   *
   * @param realtimeLogging Whether to enable real-time recording of classgraph
   * @return Aquiver
   */
  public Aquiver realtimeLogging(boolean realtimeLogging) {
    this.realtimeLogging = realtimeLogging;
    this.environment.add(PATH_SCANNER_LOGGING, realtimeLogging);
    return this;
  }

  /**
   * Get Whether to enable real-time recording of classgraph
   *
   * @return Whether to enable real-time recording of classgraph
   */
  public boolean realtimeLogging() {
    return requireNonNull(this.environment.getBoolean(PATH_SCANNER_LOGGING, realtimeLogging));
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
   * Register websocket route
   *
   * @param path             Websocket route path
   * @param webSocketChannel WebSocket abstract interface
   * @return
   */
  public Aquiver websocket(String path, WebSocketChannel webSocketChannel) {
    requireNonNull(path, "WebSocket url path can't be null");
    requireNonNull(webSocketChannel, "WebSocketChannel can't be null");

    this.routeManager.addWebSocket(path, webSocketChannel);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.GET);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.POST);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.HEAD);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.PUT);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.PATCH);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.DELETE);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.OPTIONS);
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
    this.routeManager.addRoute(path, requestHandler, HttpMethod.TRACE);
    return this;
  }

  /**
   * Register route
   *
   * @param path           Route path
   * @param requestHandler Request handler
   * @return this
   */
  public Aquiver route(String path, RequestHandler requestHandler, HttpMethod httpMethod){
    this.routeManager.addRoute(path, requestHandler, httpMethod);
    return this;
  }

  /**
   * Open an http service, which is implemented by netty by default
   *
   * @param bootClass start up class
   * @param args      main method param array
   */
  public void start(Class<?> bootClass, String[] args) {
    try {
      this.loadConfig(args);
      this.initReusableThreadPool();
    } catch (IllegalAccessException e) {
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
   * Load configuration from multiple places between startup services.
   * Support items are: Properties are configured by default, and the
   * properties loaded by default are application.properties If there
   * is no properties configuration, the yaml format is used, and the
   * default yaml loaded is application.yml Support loading
   * configuration from args array of main function Support loading
   * configuration from System.Property
   *
   * @param args main method args
   * @throws IllegalAccessException IllegalAccessException
   */
  private void loadConfig(String[] args) throws IllegalAccessException {
    String bootConf = environment().get(PATH_SERVER_BOOT_CONFIG, PATH_CONFIG_PROPERTIES);
    Environment bootConfEnv = Environment.of(bootConf);

    Map<String, String> argsMap = this.loadMainArgs(args);
    Map<String, String> constField = PropertyUtils.confFieldMap();

    this.loadPropsOrYaml(bootConfEnv, constField);

    /** Support loading configuration from args array of main function. */
    if (!requireNonNull(bootConfEnv).isEmpty()) {
      Map<String, String> bootEnvMap = bootConfEnv.toStringMap();
      Set<Map.Entry<String, String>> entrySet = bootEnvMap.entrySet();

      entrySet.forEach(entry -> this.environment
              .add(entry.getKey(), entry.getValue()));

      this.masterConfig = true;
    }

    if (Objects.nonNull(argsMap.get(PATH_SERVER_PROFILE))) {
      String envNameArg = argsMap.get(PATH_SERVER_PROFILE);
      this.envConfig(envNameArg);
      this.envName = envNameArg;
      argsMap.remove(PATH_SERVER_PROFILE);
      this.envConfig = true;
    }

    if (!envConfig) {
      String profileName = this.environment.get(PATH_SERVER_PROFILE);
      if (Objects.nonNull(profileName) && !"".equals(profileName)) {
        envConfig(profileName);
        this.envName = profileName;
      }
    }
  }

  /**
   * load properties and yaml
   *
   * @param bootConfEnv Environment used when the server starts
   * @param constField  Constant attribute map
   */
  private void loadPropsOrYaml(Environment bootConfEnv, Map<String, String> constField) {
    /** Properties are configured by default, and the properties loaded
     * by default are application.properties */
    constField.keySet().forEach(key ->
            Optional.ofNullable(System.getProperty(constField.get(key)))
                    .ifPresent(property -> bootConfEnv.add(key, property)));

    /** If there is no properties configuration, the yaml format is
     * used, and the default yaml loaded is application.yml */
    if (bootConfEnv.isEmpty()) {
      Optional.ofNullable(PropertyUtils.yaml(PATH_CONFIG_YAML))
              .ifPresent(yamlConfigTreeMap ->
                      bootConfEnv.load(new StringReader(
                              PropertyUtils.toProperties(yamlConfigTreeMap))));
    }
  }

  /**
   * Load main function parameters, and override if main configuration exists
   *
   * @param args String parameter array of main method
   * @return Write the parameters to the map and return
   */
  private Map<String, String> loadMainArgs(String[] args) {
    Map<String, String> argsMap = PropertyUtils.parseArgs(args);
    if (argsMap.size() > 0) {
      log.info("Entered command line:{}", argsMap.toString());
    }

    for (Map.Entry<String, String> next : argsMap.entrySet()) {
      this.environment.add(next.getKey(), next.getValue());
    }
    return argsMap;
  }

  /**
   * Load the environment configuration, if it exists in the main
   * configuration, it will be overwritten in the environment
   * configuration
   *
   * @param envName Environment name
   */
  private void envConfig(String envName) {
    String envFileName = "application" + "-" + envName + ".properties";
    Environment customerEnv = Environment.of(envFileName);
    if (customerEnv.isEmpty()) {
      String envYmlFileName = "application" + "-" + envName + ".yml";
      customerEnv = Environment.of(envYmlFileName);
    }
    if (!customerEnv.isEmpty()) {
      customerEnv.props().forEach((key, value) ->
              this.environment.add(key.toString(), value));
    }
    this.environment.add(PATH_SERVER_PROFILE, envName);
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
