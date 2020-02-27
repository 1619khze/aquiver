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
package org.aquiver.server.banner;

import org.aquiver.Const;

import java.io.PrintStream;

/**
 * @author WangYi
 * @since 2019/5/13
 */
public abstract class BannerTemplate implements Banner {

  public abstract void prePrintBannerText(PrintStream printStream, String bannerText, String bannerFont);

  public abstract String setUpPadding(Integer strapLineSize);

  public abstract void printTextAndVersion(PrintStream printStream, String padding);

  @Override
  public void printBanner(PrintStream printStream, String bannerText, String bannerFont) {
    this.prePrintBannerText(printStream, bannerText, bannerFont);
    final String padding = setUpPadding(Const.STRAP_LINE_SIZE);
    this.printTextAndVersion(printStream, padding);
  }
}
