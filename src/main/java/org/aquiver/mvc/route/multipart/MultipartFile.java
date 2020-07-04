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
package org.aquiver.mvc.route.multipart;

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author WangYi
 * @since 2020/6/14
 */
public class MultipartFile {
  private static final Logger log = LoggerFactory.getLogger(MultipartFile.class);

  private String fileName;
  private InputStream inputStream;
  private Charset charset;
  private String charsetName;
  private String contentType;
  private String contentTransferEncoding;
  private File file;
  private String path;
  private long length;
  private ChannelHandlerContext context;

  public String readFileContent() throws IOException {
    return new String(Files.readAllBytes(file.toPath()));
  }

  public void download(String path) {
    File file = new File(path);
    try {
      final RandomAccessFile raf = new RandomAccessFile(file, "r");
      long fileLength = raf.length();
      HttpResponse response = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
      response.headers().set(HttpHeaderNames.CONTENT_LENGTH, fileLength);
      response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/octet-stream");
      response.headers().add(HttpHeaderNames.CONTENT_DISPOSITION,
              String.format("attachment; filename=\"%s\"", file.getName()));

      this.context.write(response);

      ChannelFuture sendFileFuture = context.write(new DefaultFileRegion(raf.getChannel(),
              0, fileLength), context.newProgressivePromise());

      sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
        @Override
        public void operationComplete(ChannelProgressiveFuture future)
                throws Exception {
          log.info("file {} transfer complete.", file.getName());
          raf.close();
        }

        @Override
        public void operationProgressed(ChannelProgressiveFuture future,
                                        long progress, long total) {
          if (total < 0) {
            log.warn("file {} transfer progress: {}", file.getName(), progress);
          } else {
            log.debug("file {} transfer progress: {}/{}", file.getName(), progress, total);
          }
        }
      });
      this.context.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
    } catch (IOException e) {
      log.warn("file {} not found", file.getPath());
    }
  }

  public String fileName() {
    return fileName;
  }

  public void fileName(String fileName) {
    this.fileName = fileName;
  }

  public InputStream inputStream() {
    return inputStream;
  }

  public void inputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Charset charset() {
    return charset;
  }

  public void charset(Charset charset) {
    this.charset = charset;
  }

  public String charsetName() {
    return charsetName;
  }

  public void charsetName(String charsetName) {
    this.charsetName = charsetName;
  }

  public String contentType() {
    return contentType;
  }

  public void contentType(String contentType) {
    this.contentType = contentType;
  }

  public String contentTransferEncoding() {
    return contentTransferEncoding;
  }

  public void contentTransferEncoding(String contentTransferEncoding) {
    this.contentTransferEncoding = contentTransferEncoding;
  }

  public File file() {
    return file;
  }

  public void file(File file) {
    this.file = file;
  }

  public String path() {
    return path;
  }

  public void path(String path) {
    this.path = path;
  }

  public long length() {
    return length;
  }

  public void length(long length) {
    this.length = length;
  }

  public void channelContext(ChannelHandlerContext context) {
    this.context = context;
  }
}
