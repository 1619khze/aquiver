package org.aquiver.result.view;

import io.netty.handler.codec.http.FullHttpResponse;
import org.apex.Environment;
import org.apex.io.FileBaseResource;
import org.apex.io.Resource;
import org.aquiver.Aquiver;
import org.aquiver.Const;
import org.aquiver.RequestContext;
import org.aquiver.result.ResultUtils;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author WangYi
 * @since 2020/8/27
 */
public class HtmlTemplateViewHandler extends AbstractTemplateViewHandler {
  private final Environment environment = Aquiver.of().environment();

  @Override
  public String getPrefix() {
    return environment.get(Const.PATH_SERVER_VIEW_PREFIX, "");
  }

  @Override
  public String getSuffix() {
    return environment.get(Const.PATH_SERVER_VIEW_SUFFIX, Const.SERVER_VIEW_SUFFIX);
  }

  @Override
  protected void doRender(RequestContext ctx, String viewPathName) throws Exception {
    viewPathName = viewPathName + "." + getSuffix();
    URL viewUrl = this.getClass().getClassLoader().getResource(viewPathName);
    Resource resource = new FileBaseResource(Paths.get(viewUrl.toURI()));
    String htmlContent = new String(Files.readAllBytes(resource.getPath()));
    FullHttpResponse response = ResultUtils.contentResponse(htmlContent);
    ctx.writeAndFlush(response);
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
