package org.aquiver;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import org.aquiver.route.session.HttpSession;
import org.aquiver.route.session.Session;
import org.aquiver.route.session.SessionManager;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Request class based on netty-based Full Http Request secondary packaging
 *
 * @author WangYi
 * @since 2020/6/25
 */
public class Request {
  /** Save the query string for this request */
  private static final Map<String, String> queryStringMap = new HashMap<>();
  /** Save the cookies for this request */
  private static final Map<String, Object> cookieMap = new HashMap<>();
  /** Save the form data for this request */
  private static final Map<String, Object> formDataMap = new HashMap<>();
  /** Save the json data for this request */
  private static final Map<String, Object> jsonDataMap = new HashMap<>();
  /** Save the fileUpload for this request */
  private static final Map<String, FileUpload> fileUploadMap = new HashMap<>();

  /** Atomic boolean variable to judge whether parameter is initialized */
  private final AtomicBoolean initQueryString = new AtomicBoolean(false);
  private final AtomicBoolean initCookie = new AtomicBoolean(false);
  private final AtomicBoolean initJsonData = new AtomicBoolean(false);
  private final AtomicBoolean initFormData = new AtomicBoolean(false);
  private final AtomicBoolean initFileUpload = new AtomicBoolean(false);

  /** Query string decoder */
  private QueryStringDecoder queryStringDecoder;
  /** Post request decoder */
  private HttpPostRequestDecoder httpPostRequestDecoder;

  /**
   * Combine the {@link HttpRequest} and {@link FullHttpMessage}, so the request is a <i>complete</i> HTTP
   * request.
   */
  private final FullHttpRequest httpRequest;
  private final ChannelHandlerContext ctx;

  /**
   * Used to save the session state between the client and the server
   */
  private final SessionManager sessionManager = new SessionManager();

  private static final String mediaTypeKey = "Content-Type";

  /**
   * Constructor, pass in Full Http Request and Channel Handler Context for initialization
   *
   * @param fullHttpRequest The complete http request interface provided by netty
   * @param ctx             Context class provided by netty for various operations of Channel Handler
   */
  public Request(FullHttpRequest fullHttpRequest, ChannelHandlerContext ctx) {
    this.httpRequest = fullHttpRequest;
    this.ctx = ctx;
    final HttpSession httpSession = new HttpSession(channelHandlerContext().channel());
    sessionManager.addSession(httpSession);
  }

  public FullHttpRequest httpRequest() {
    return httpRequest;
  }

  public ChannelHandlerContext channelHandlerContext() {
    return ctx;
  }

  /**
   * Get cookies map
   *
   * @return cookies map
   */
  public Map<String, Object> cookies() {
    if (initCookie.get()) {
      return cookieMap;
    }

    initCookie.set(true);

    String cookieString = httpRequest.headers()
            .get(HttpHeaderNames.COOKIE);

    if (Objects.nonNull(cookieString)) {
      Set<Cookie> cookies = ServerCookieDecoder
              .STRICT.decode(cookieString);

      if (cookies.isEmpty()) {
        return cookieMap;
      }
      for (Cookie cookie : cookies) {
        cookieMap.put(cookie.name(), cookie.value());
      }
    }
    return cookieMap;
  }

  /**
   * Get cookie value according to key
   *
   * @param key cookie key
   * @return cookie value
   */
  public Object cookies(String key) {
    return cookies().get(key);
  }

  /**
   * Get all cookie keys
   *
   * @return all cookie keys
   */
  public Set<String> cookiesKey() {
    return cookies().keySet();
  }

  /**
   * Get form data
   *
   * @return form data map
   */
  public Map<String, Object> formData() {
    if (initFormData.get()) {
      return formDataMap;
    }

    initFormData.set(true);

    if (Objects.isNull(httpPostRequestDecoder)) {
      /** Interface to enable creation of InterfaceHttpData objects */
      HttpDataFactory httpDataFactory = new DefaultHttpDataFactory(true);
      this.httpPostRequestDecoder = new HttpPostRequestDecoder(httpDataFactory, httpRequest);
    }

    for (InterfaceHttpData data : this.httpPostRequestDecoder.getBodyHttpDatas()) {
      if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
        MemoryAttribute attribute = (MemoryAttribute) data;
        formDataMap.put(attribute.getName(), attribute.getValue());
      }
    }
    return formDataMap;
  }

  /**
   * Get the value of form data according to key
   *
   * @param key form data key
   * @return form data value
   */
  public Object formData(String key) {
    return formData().get(key);
  }

  /**
   * Get all keys of form data
   *
   * @return all keys of form data
   */
  public Set<String> formDataKeys() {
    return formData().keySet();
  }

  /**
   * Get files in form data
   * return files map
   */
  public Map<String, FileUpload> fileUpload() {
    if (initFileUpload.get()) {
      return fileUploadMap;
    }
    initFileUpload.set(true);

    for (InterfaceHttpData data : this.httpPostRequestDecoder.getBodyHttpDatas()) {
      if (InterfaceHttpData.HttpDataType.FileUpload.equals(data.getHttpDataType())) {
        final FileUpload fileUpload = (FileUpload) data;
        fileUploadMap.put(fileUpload.getName(), fileUpload);
      }
    }
    return fileUploadMap;
  }

  /**
   * Get FileUpload based on key
   *
   * @param key file key
   * @return FileUpload
   */
  public FileUpload fileUpload(String key) {
    return fileUpload().get(key);
  }

  /**
   * Get file key in form data
   *
   * @return file keys
   */
  public Set<String> fileUploadKeys() {
    return fileUpload().keySet();
  }

  /**
   * The json in the acquisition request is data, which
   * can be obtained when the Media Type is application/json
   *
   * @return Alibaba JSONObject
   */
  public JSONObject json() {
    if (initJsonData.get()) {
      return new JSONObject(jsonDataMap);
    }

    initJsonData.set(true);

    Object contentType = headers().get(mediaTypeKey);
    if (!Objects.isNull(contentType) &&
            String.valueOf(contentType)
                    .equals(MediaType.APPLICATION_JSON_VALUE)) {

      JSONObject jsonParams = JSONObject.parseObject(getContent());
      for (Object key : jsonParams.keySet()) {
        jsonParams.put(key.toString(), jsonParams.get(key.toString()));
      }
      jsonDataMap.putAll(jsonParams);
    }
    return new JSONObject(jsonDataMap);
  }

  /**
   * Get the requested content
   *
   * @return content string
   */
  private String getContent() {
    ByteBuf content = httpRequest.content();
    byte[] reqContent = new byte[content.readableBytes()];
    content.readBytes(reqContent);
    return new String(reqContent, StandardCharsets.UTF_8);
  }

  /**
   * Convert its json to map
   *
   * @return json to map
   */
  public Map<String, Object> jsonToMap() {
    return json().getInnerMap();
  }

  /**
   * Get json data by key
   *
   * @param key json key
   * @return json value
   */
  public Object json(String key) {
    return json().get(key);
  }

  /**
   * Get all keys in json data
   *
   * @return json data all keys
   */
  public Set<String> jsonKeys() {
    return json().keySet();
  }

  /**
   * Returns the requested URI (or alternatively, path)
   *
   * @return The URI being requested
   */
  public String uri() {
    return httpRequest.uri();
  }

  /**
   * Returns the {@link HttpMethod} of this {@link HttpRequest}.
   *
   * @return The {@link HttpMethod} of this {@link HttpRequest}
   */
  public HttpMethod httpMethod() {
    return httpRequest.method();
  }

  /**
   * Returns the {@link HttpMethod} of this {@link HttpRequest} name.
   *
   * @return The {@link HttpMethod} of this {@link HttpRequest} name.
   */
  public String httpMethodName() {
    return httpRequest.method().name();
  }

  /**
   * Returns the protocol version of this {@link HttpMessage}
   *
   * @return Returns the protocol version of this {@link HttpMessage}
   */
  public HttpVersion protocolVersion() {
    return httpRequest.protocolVersion();
  }

  /**
   * Returns the name of the protocol such as {@code "HTTP"} in {@code "HTTP/1.0"}.
   *
   * @return Returns the name of the protocol such as {@code "HTTP"} in {@code "HTTP/1.0"}.
   */
  public String protocolVersionName() {
    return httpRequest.protocolVersion().protocolName();
  }

  /**
   * Get request query string according to Key
   *
   * @param key Key to get param
   * @return Key corresponding parameters
   */
  public Object queryString(String key) {
    return queryStrings().get(key);
  }

  /**
   * Get the keys of all query strings
   *
   * @return The keys of all query strings
   */
  public Object queryStringKeys() {
    return queryStrings().keySet();
  }

  /**
   * Map that returns the query string
   *
   * @return Map of query string
   */
  public Map<String, String> queryStrings() {
    if (initQueryString.get()) {
      return queryStringMap;
    }

    initQueryString.set(true);
    this.queryStringDecoder = Optional.ofNullable(this.queryStringDecoder)
            .orElseGet(() -> new QueryStringDecoder(httpRequest.uri()));

    Map<String, List<String>> params = this.queryStringDecoder.parameters();
    for (Map.Entry<String, List<String>> p : params.entrySet()) {
      String key = p.getKey();
      List<String> value = p.getValue();
      for (String val : value) {
        queryStringMap.put(key, val);
      }
    }
    return queryStringMap;
  }

  /**
   * Get request header information based on key
   *
   * @param key headers key
   * @return headers value
   */
  public Object headers(String key) {
    return headers().get(key);
  }

  /**
   * Get headers
   *
   * @return http headers
   */
  public HttpHeaders headers() {
    return httpRequest.headers();
  }

  /**
   * Get session attributes
   *
   * @param key session key
   * @return session attributes
   */
  public Object sessionAttribute(String key) {
    return session().getAttribute(key);
  }

  /**
   * Get all Session key
   *
   * @return all session key
   */
  public List<String> attributeNames() {
    return session().getAttributeNames();
  }

  /**
   * Set session attribute
   *
   * @param key   session key
   * @param value session vaue
   */
  public void attribute(String key, Object value) {
    session().setAttribute(key, value);
  }

  /**
   * 获取session对象
   *
   * @return Request session
   */
  public Session session() {
    return sessionManager.session(ctx.channel().id().asLongText());
  }
}
