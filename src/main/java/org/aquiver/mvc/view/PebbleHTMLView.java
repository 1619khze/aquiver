package org.aquiver.mvc.view;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;

/**
 * @author WangYi
 * @since 2020/6/17
 */
public class PebbleHTMLView implements HTMLView {
  private final PebbleEngine engine;

  public PebbleHTMLView() {
    this.engine = new PebbleEngine.Builder().build();
  }

  /**
   * Get the support view type.
   *
   * @return ViewType
   */
  @Override
  public ViewType viewType() {
    return ViewType.HTML;
  }

  /**
   * Get the content of the returned view
   *
   * @param htmlPath   html path
   * @param viewParams view render params
   * @return the content of the returned view
   * @throws IOException io exception
   */
  @Override
  public String renderView(String htmlPath, Map<String, Object> viewParams) throws IOException {
    final PebbleTemplate compiledTemplate = engine.getTemplate(htmlPath);
    final Writer writer = new StringWriter();
    compiledTemplate.evaluate(writer, viewParams);
    return writer.toString();
  }
}
