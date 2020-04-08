package org.aquiver.mvc;

public enum RequestParamType {

  /**
   * Request Url 参数
   */
  REQUEST_PARAM,

  /**
   * 路径变量
   */
  PATH_VARIABLE,

  /**
   * Http Request
   */
  HTTP_REQUEST,

  /**
   * Http Response
   */
  HTTP_RESPONSE,

  /**
   * 请求体
   */
  REQUEST_BODY,

  /**
   * X-WWW-FORM-URLENCODED
   */
  URL_ENCODED_FORM,

  /**
   * Cookies
   */
  REQUEST_COOKIES,

  /**
   * Http Request Header参数
   */
  REQUEST_HEADER,

  /**
   * 上传文件
   */
  UPLOAD_FILE,

  /**
   * 多个上传文件
   */
  UPLOAD_FILES
}
