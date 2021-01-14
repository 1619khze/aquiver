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
package org.aquiver.server;

import io.leego.banana.BananaUtils;
import org.aquiver.ServerSpec;
import org.aquiver.server.ansi.AnsiColor;
import org.aquiver.server.ansi.AnsiOutput;
import org.aquiver.server.banner.BannerTemplate;

import java.io.PrintStream;

/**
 * @author WangYi
 * @since 2019/5/13
 */
public class NettyServerBanner extends BannerTemplate {

  @Override
  public void prePrintBannerText(PrintStream printStream, String bannerText, String bannerFont) {
    printStream.println(BananaUtils.bananaify(bannerText, bannerFont));
  }

  @Override
  public String setUpPadding(Integer strapLineSize) {
    final StringBuilder padding = new StringBuilder();
    while (padding.length() < strapLineSize - (ServerSpec.AQUIVER_VERSION.length() + ServerSpec.AQUIVER_FRAMEWORK.length())) {
      padding.append(ServerSpec.SPACE);
    }
    return padding.toString();
  }

  @Override
  public void printTextAndVersion(PrintStream printStream, String padding) {
    printStream.println(AnsiOutput.toString(AnsiColor.GREEN, ServerSpec.AQUIVER_FRAMEWORK,
            AnsiColor.RESET, padding, ServerSpec.AQUIVER_VERSION));
    printStream.println();
  }
}
