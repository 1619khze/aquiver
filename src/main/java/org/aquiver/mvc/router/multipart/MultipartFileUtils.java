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
package org.aquiver.mvc.router.multipart;

import io.netty.channel.*;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public final class MultipartFileUtils {
  /**
   * Build Multipart File
   *
   * @param fileUpload FileUpload interface that could be in
   *                   memory, on temporary file or any other implementations.
   * @return MultipartFile
   * @throws IOException Thrown when there is a problem getting the file
   */
  public static MultipartFile createMultipartFile(FileUpload fileUpload, ChannelHandlerContext ctx) throws IOException {
    final MultipartFile multipartFile = new MultipartFile();
    multipartFile.charset(fileUpload.getCharset());
    multipartFile.charsetName(fileUpload.getCharset().name());
    multipartFile.contentTransferEncoding(fileUpload.getContentTransferEncoding());
    multipartFile.contentType(fileUpload.getContentType());
    multipartFile.fileName(fileUpload.getFilename());
    multipartFile.channelContext(ctx);

    Path tmpFile = Files.createTempFile(
            Paths.get(fileUpload.getFile().getParent()), "aquiver", "_upload");

    Path fileUploadPath = Paths.get(fileUpload.getFile().getPath());
    Files.move(fileUploadPath, tmpFile, StandardCopyOption.REPLACE_EXISTING);

    multipartFile.file(tmpFile.toFile());
    multipartFile.inputStream(new FileInputStream(multipartFile.file()));
    multipartFile.path(tmpFile.toFile().getPath());
    multipartFile.length(fileUpload.length());
    return multipartFile;
  }

  public static ChannelFuture writeChunkFile(ChannelHandlerContext context, RandomAccessFile raf) throws IOException {
    ChannelFuture sendFileFuture;
    long length = raf.length();
    if (context.pipeline().get(SslHandler.class) == null) {
      sendFileFuture = context.write(new DefaultFileRegion(raf.getChannel(),
              0, length), context.newProgressivePromise());
    } else {
      sendFileFuture = context.write(new HttpChunkedInput(new ChunkedFile(raf,
              0, length, 8192)), context.newProgressivePromise());
    }
    return sendFileFuture;
  }

  public static void closeChannel(RandomAccessFile raf, ChannelFuture sendFileFuture) {
    sendFileFuture.addListener(new ChannelProgressiveFutureListener() {
      @Override
      public void operationComplete(ChannelProgressiveFuture future) throws Exception {
        raf.close();
      }

      @Override
      public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
      }
    });
  }
}
