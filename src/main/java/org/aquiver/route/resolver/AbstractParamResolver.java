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
package org.aquiver.route.resolver;

import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.route.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/6/27
 */
public abstract class AbstractParamResolver {

  /**
   * Build Multipart File
   *
   * @param fileUpload FileUpload interface that could be in
   *                   memory, on temporary file or any other implementations.
   * @return MultipartFile
   * @throws IOException Thrown when there is a problem getting the file
   */
  protected MultipartFile buildMultipartFile(FileUpload fileUpload) throws IOException {
    final MultipartFile multipartFile = new MultipartFile();
    multipartFile.setCharset(fileUpload.getCharset());
    multipartFile.setCharsetName(fileUpload.getCharset().name());
    multipartFile.setContentTransferEncoding(fileUpload.getContentTransferEncoding());
    multipartFile.setContentType(fileUpload.getContentType());
    multipartFile.setFileName(fileUpload.getFilename());

    Path tmpFile = Files.createTempFile(
            Paths.get(fileUpload.getFile().getParent()), "aquiver", "_upload");

    Path fileUploadPath = Paths.get(fileUpload.getFile().getPath());
    Files.move(fileUploadPath, tmpFile, StandardCopyOption.REPLACE_EXISTING);

    multipartFile.setFile(tmpFile.toFile());
    multipartFile.setInputStream(new FileInputStream(multipartFile.getFile()));
    multipartFile.setPath(tmpFile.toFile().getPath());
    multipartFile.setLength(fileUpload.length());
    return multipartFile;
  }

  /**
   * Determine if it is a collection
   *
   * @param cls class
   * @return Is it collection
   */
  protected boolean isCollection(Class<?> cls) {
    return Collection.class.isAssignableFrom(cls);
  }

  /**
   * Determine whether it is Map
   *
   * @param cls class
   * @return Is it map
   */
  protected boolean isMap(Class<?> cls) {
    return Map.class.isAssignableFrom(cls);
  }
}
