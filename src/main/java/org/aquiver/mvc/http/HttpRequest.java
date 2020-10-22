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
package org.aquiver.mvc.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import org.apache.commons.lang3.Validate;
import org.aquiver.Aquiver;
import org.aquiver.Request;
import org.aquiver.mvc.router.session.Session;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Request class based on netty-based Full Http Request secondary packaging
 *
 * @author WangYi
 * @since 2020/6/25
 */
public class HttpRequest implements Request {

  private static final ByteBuf EMPTY_BUF =
          Unpooled.copiedBuffer("", CharsetUtil.UTF_8);

  private static final HttpDataFactory HTTP_DATA_FACTORY =
          new DefaultHttpDataFactory(true); // Disk if size exceed

  private final Map<String, Object> httpData = new HashMap<>();
  private final String sessionKey = Aquiver.of().sessionKey();
  private final FullHttpRequest nettyRequest;
  private final ChannelHandlerContext context;
  private final Map<String, Cookie> cookies;
  private final Map<String, Header> headers;
  private final Map<String, FileUpload> fileUploads;
  private final QueryStringDecoder paramDecoder;
  private final HttpPostRequestDecoder postDecoder;
  private ByteBuf body = EMPTY_BUF;
  private Session session;

  private HttpRequest(FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
    this.nettyRequest = fullHttpRequest;
    this.context = context;
    this.cookies = Cookies.parse(nettyRequest.headers());
    this.headers = Headers.parse(nettyRequest.headers());
    this.fileUploads = new HashMap<>();
    this.paramDecoder = new QueryStringDecoder(nettyRequest.uri());
    this.postDecoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY, nettyRequest);
    this.init();
  }

  public static HttpRequest of(FullHttpRequest fullHttpRequest, ChannelHandlerContext context) {
    Validate.notNull(fullHttpRequest, "FullHttpRequest can't be null");
    Validate.notNull(context, "ChannelHandlerContext can't be null");

    return new HttpRequest(fullHttpRequest, context);
  }

  private void init() {
    final Map<String, List<String>> parameters = this.paramDecoder.parameters();
    for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
      final String key = entry.getKey();
      final List<String> value = entry.getValue();
      httpData.put(key, value.size() == 0 ? null : value.get(0));
    }

    if (Aquiver.of().sessionEnable()) {
      this.session = Aquiver.of().sessionManager()
              .createSession(this, context);
    }
    this.body = nettyRequest.content();

    final Object contentType = header("Content-Type");
    if (!Objects.isNull(contentType) && body != null
            && String.valueOf(contentType)
            .equals(MediaType.APPLICATION_JSON_VALUE)) {

      final byte[] reqContent = new byte[body.readableBytes()];
      this.body.readBytes(reqContent);
      final String bodyContent = new String(reqContent, StandardCharsets.UTF_8);

      final JSONObject jsonParams = JSONObject.parseObject(bodyContent);
      for (Object key : jsonParams.keySet()) {
        jsonParams.put(key.toString(), jsonParams.get(key.toString()));
      }
      this.httpData.putAll(jsonParams);
    }

    try {
      this.postDecoder.offer(nettyRequest);
      final List<InterfaceHttpData> bodyHttpDatas = postDecoder.getBodyHttpDatas();
      for (InterfaceHttpData httpData : bodyHttpDatas) {
        if (InterfaceHttpData.HttpDataType.FileUpload.equals(httpData.getHttpDataType())) {
          final FileUpload fileUpload = (FileUpload) httpData;
          this.fileUploads.put(fileUpload.getName(), fileUpload);
        } else {
          final Attribute data = (Attribute) httpData;
          this.httpData.put(data.getName(), data.getValue());
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("resolver request exception", e);
    }
  }

  private InetSocketAddress getInetSocketAddress() {
    return (InetSocketAddress) context.channel().remoteAddress();
  }

  private HttpVersion getHttpVersion() {
    return this.nettyRequest.protocolVersion();
  }

  @Override
  public Map<String, FileUpload> fileUploads() {
    return this.fileUploads;
  }

  @Override
  public FileUpload fileUploads(String fileKey) {
    return this.fileUploads().get(fileKey);
  }

  @Override
  public Boolean isMultipart() {
    return this.postDecoder.isMultipart();
  }

  @Override
  public ByteBuf body() {
    return this.body;
  }

  @Override
  public <T> T body(Class<T> type) {
    String jsonString = JSON.toJSONString(httpData);
    if (httpData.isEmpty() || !JSONObject.isValid(jsonString)) {
      return null;
    }
    return JSONObject.parseObject(jsonString, type);
  }

  @Override
  public Session session() {
    return this.session;
  }

  @Override
  public String method() {
    return this.nettyRequest.method().name();
  }

  @Override
  public String protocol() {
    return getHttpVersion().protocolName();
  }

  @Override
  public Integer minorVersion() {
    return getHttpVersion().minorVersion();
  }

  @Override
  public Integer majorVersion() {
    return getHttpVersion().majorVersion();
  }

  @Override
  public Header header(String key) {
    return header().get(key);
  }

  @Override
  public String param(String key) {
    return String.valueOf(httpData.get(key));
  }

  @Override
  public Set<String> paramNames() {
    return this.paramDecoder.parameters().keySet();
  }

  @Override
  public Set<String> headerNames() {
    return header().keySet();
  }

  @Override
  public String uri() {
    return this.nettyRequest.uri();
  }

  @Override
  public String path() {
    return this.paramDecoder.path();
  }

  @Override
  public String rawPath() {
    return this.paramDecoder.rawPath();
  }

  @Override
  public String rawQuery() {
    return this.paramDecoder.rawQuery();
  }

  @Override
  public Integer port() {
    InetSocketAddress inSocket = getInetSocketAddress();
    return inSocket.getPort();
  }

  @Override
  public String remoteAddress() {
    InetSocketAddress inSocket = getInetSocketAddress();
    return inSocket.getAddress().getHostAddress();
  }

  @Override
  public String host() {
    return header().get(HttpConst.HOST).getString();
  }

  @Override
  public String accept() {
    return header().get(HttpConst.ACCEPT).getString();
  }

  @Override
  public String connection() {
    return header().get(HttpConst.CONNECTION).getString();
  }

  @Override
  public String referer() {
    return header().get(HttpConst.REFERER).getString();
  }

  @Override
  public String userAgent() {
    return header().get(HttpConst.USER_AGENT).getString();
  }

  @Override
  public Map<String, Header> header() {
    return requireNonNull(this.headers);
  }

  @Override
  public Boolean isKeepAlive() {
    return HttpUtil.isKeepAlive(nettyRequest);
  }

  @Override
  public Boolean is100ContinueExpected() {
    return HttpUtil.is100ContinueExpected(nettyRequest);
  }

  @Override
  public Map<String, Cookie> cookies() {
    return this.cookies;
  }

  @Override
  public Cookie cookie(String key) {
    return this.cookies().get(key);
  }

  @Override
  public String sessionKey() {
    return this.sessionKey;
  }

  @Override
  public String toString() {
    return "Request{" +
            "httpData=" + httpData +
            ", nettyRequest=" + nettyRequest +
            ", context=" + context +
            ", sessionKey='" + sessionKey + '\'' +
            ", cookies=" + cookies +
            ", header=" + headers +
            ", queryStringDecoder=" + paramDecoder +
            ", postRequestDecoder=" + postDecoder +
            ", session=" + session +
            '}';
  }
}
