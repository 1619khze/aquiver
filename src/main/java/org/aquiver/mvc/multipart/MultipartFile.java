package org.aquiver.mvc.multipart;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * @author WangYi
 * @since 2020/6/14
 */
public class MultipartFile {
  private String fileName;
  private InputStream inputStream;
  private Charset charset;
  private String charsetName;
  private String contentType;
  private String contentTransferEncoding;
  private File file;
  private String path;
  private long length;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public InputStream getInputStream() {
    return inputStream;
  }

  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public Charset getCharset() {
    return charset;
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  public String getCharsetName() {
    return charsetName;
  }

  public void setCharsetName(String charsetName) {
    this.charsetName = charsetName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getContentTransferEncoding() {
    return contentTransferEncoding;
  }

  public void setContentTransferEncoding(String contentTransferEncoding) {
    this.contentTransferEncoding = contentTransferEncoding;
  }

  public File getFile() {
    return file;
  }

  public void setFile(File file) {
    this.file = file;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public long getLength() {
    return length;
  }

  public void setLength(long length) {
    this.length = length;
  }
}
