package org.aquiver.result.view;

import org.aquiver.RequestContext;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public class HtmlTemplateViewHandler extends AbstractTemplateViewHandler {
  @Override
  public String getPrefix() {
    return null;
  }

  @Override
  protected void doRender(RequestContext ctx, String viewPathName) {

  }

  @Override
  public String getType() {
    return "html";
  }

  @Override
  public ViewHandlerType getHandlerType() {
    return ViewHandlerType.TEMPLATE_VIEW;
  }
}
