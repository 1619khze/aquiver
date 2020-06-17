package org.aquiver.mvc;

import java.util.Map;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class ModelAndView {
  private Map<String, Object> params;
  private String htmlPath;

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public String getHtmlPath() {
    return htmlPath;
  }

  public void setHtmlPath(String htmlPath) {
    this.htmlPath = htmlPath;
  }

  public ModelAndView(Map<String, Object> params, String htmlPath) {
    this.params = params;
    this.htmlPath = htmlPath;
  }

  @Override
  public String toString() {
    return "ModelAndView{" +
            "params=" + params +
            ", htmlPath='" + htmlPath + '\'' +
            '}';
  }
}
