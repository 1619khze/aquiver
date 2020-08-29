package org.aquiver.mvc.router.multipart;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.multipart.FileUpload;

import java.io.FileInputStream;
import java.io.IOException;
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
}
