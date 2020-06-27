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
package org.aquiver.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.aquiver.RequestContext;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class AbstractExceptionHandler implements ExceptionHandler, HttpSupport {
  /**
   * Returns the {@link HttpResponseStatus} represented by the specified code.
   * If the specified code is a standard HTTP status code, a cached instance
   * will be returned.  Otherwise, a new instance will be returned.
   */
  public boolean support(int code) {
    return !Objects.isNull(HttpResponseStatus.valueOf(code));
  }

  /**
   * Returns the {@link HttpResponseStatus} represented by the specified {@code code} and {@code reasonPhrase}.
   * If the specified code is a standard HTTP status {@code code} and {@code reasonPhrase}, a cached instance
   * will be returned. Otherwise, a new instance will be returned.
   *
   * @param code         The response code value.
   * @param reasonPhrase The response code reason phrase.
   * @return the {@link HttpResponseStatus} represented by the specified {@code code} and {@code reasonPhrase}.
   */
  @Override
  public boolean support(int code, String reasonPhrase) {
    return !Objects.isNull(HttpResponseStatus.valueOf(code, reasonPhrase));
  }

  public ChannelFuture writeResponse(RequestContext requestContext, FullHttpResponse fullHttpResponse) {
    return requestContext.request().channelHandlerContext().writeAndFlush(fullHttpResponse);
  }

  public void closeFuture(ChannelFuture channelFuture) {
    channelFuture.addListener(ChannelFutureListener.CLOSE);
  }

  /**
   * Build FullHttpResponse
   *
   * @param throwable exception
   * @return FullHttpResponse
   */
  protected FullHttpResponse buildResponse(RequestContext requestContext,
                                           Throwable throwable, HttpResponseStatus status) {
    HttpVersion httpVersion = requestContext.request().protocolVersion();
    byte[] exceptionMessageByte = this.getExceptionMessageByte("Failure: " + throwable.getLocalizedMessage() + "\r\n");
    ByteBuf byteBuf = Unpooled.copiedBuffer(exceptionMessageByte);
    return new DefaultFullHttpResponse(httpVersion, status, byteBuf);
  }

  /**
   * Get byte array of exception message in standard character set encoding format
   *
   * @param message exception message
   * @return byte array of exception message in standard character set encoding format
   */
  private byte[] getExceptionMessageByte(String message) {
    return message.getBytes(StandardCharsets.UTF_8);
  }
}
