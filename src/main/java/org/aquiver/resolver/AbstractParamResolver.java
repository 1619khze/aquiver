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
package org.aquiver.resolver;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.multipart.FileUpload;
import org.aquiver.route.RouteParam;
import org.aquiver.route.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

/**
 * @author WangYi
 * @since 2020/6/27
 */
public abstract class AbstractParamResolver {
  private final List<ParamResolver> paramResolvers = new ArrayList<>();

  /**
   * Build Multipart File
   *
   * @param fileUpload FileUpload interface that could be in
   *                   memory, on temporary file or any other implementations.
   * @return MultipartFile
   * @throws IOException Thrown when there is a problem getting the file
   */
  protected MultipartFile buildMultipartFile(FileUpload fileUpload, ChannelHandlerContext ctx) throws IOException {
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

  private void initialize() throws Exception {
    ServiceLoader<ParamResolver> paramResolverLoad = ServiceLoader.load(ParamResolver.class);
    for (ParamResolver ser : paramResolverLoad) {
      ParamResolver paramResolver = ser.getClass().getDeclaredConstructor().newInstance();
      getParamResolvers().add(paramResolver);
    }
  }

  public void initialize(Set<Class<?>> classSet) throws Exception {
    for (Class<?> cls : classSet) {
      Class<?>[] interfaces = cls.getInterfaces();
      if (interfaces.length == 0) {
        continue;
      }
      for (Class<?> interfaceCls : interfaces) {
        if (!interfaceCls.equals(ParamResolver.class)) {
          continue;
        }
        ParamResolver paramResolver = (ParamResolver) cls.getDeclaredConstructor().newInstance();
        getParamResolvers().add(paramResolver);
      }
    }
    this.initialize();
  }

  public List<RouteParam> invokeParamResolver(Parameter[] ps, String[] paramNames) {
    List<RouteParam> routeParams = new ArrayList<>();
    for (int i = 0; i < ps.length; i++) {
      List<ParamResolver> paramResolvers = getParamResolvers();
      if (paramResolvers.isEmpty()) {
        break;
      }
      for (ParamResolver paramResolver : paramResolvers) {
        if (!paramResolver.support(ps[i])) continue;
        RouteParam param = paramResolver.resolve(ps[i], paramNames[i]);
        if (Objects.nonNull(param)) {
          routeParams.add(param);
        }
      }
    }
    return routeParams;
  }

  public List<ParamResolver> getParamResolvers() {
    return paramResolvers;
  }
}
