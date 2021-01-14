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
package org.aquiver;

import org.aquiver.Aquiver;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public interface ServerSpec {
  Integer SERVER_PORT = 8080;
  Integer SERVER_SESSION_TIMEOUT = 1800;
  Integer STRAP_LINE_SIZE = 42;

  Boolean SERVER_SSL = false;
  Boolean SERVER_CORS = false;
  Boolean SERVER_CONTENT_COMPRESSOR = false;
  Boolean SERVER_SESSION_ENABLE = true;

  String SERVER_ADDRESS = "localhost";
  String AQUIVER_VERSION = "(v1.0.0 RELEASE)";
  String BANNER_TEXT = "aquiver";
  String AQUIVER_FRAMEWORK = " :: Aquiver Framework :: ";
  String SPACE = " ";
  String FAVICON_PATH = "/favicon.ico";
  String SERVER_THREAD_NAME = "（'-'*)";
  String SERVER_VIEW_SUFFIX = "html";
  String SERVER_SESSION_KEY = "AQSESSION";
  String SERVER_TEMPLATES_FOLDER = "templates";
  String PATH_SERVER_PORT = "server.port";
  String PATH_SERVER_SSL = "server.ssl";
  String PATH_SERVER_ADDRESS = "server.address";
  String PATH_SERVER_CORS = "server.cors";
  String PATH_SERVER_CONTENT_COMPRESSOR = "server.autoCompression";
  String PATH_ENV_WATCHER = "server.env.watcher";
  String PATH_SERVER_VIEW_PREFIX = "server.view.prefix";
  String PATH_SERVER_VIEW_SUFFIX = "server.view.suffix";
  String PATH_SERVER_SESSION_KEY = "server.session.key";
  String PATH_SERVER_SESSION_TIMEOUT = "server.session.timeout";
  String PATH_SERVER_SESSION_ENABLE = "server.session.enable";
  String PATH_SERVER_TEMPLATES_FOLDER = "server.template.folder";

  // netty setting
  String PATH_SERVER_SSL_CERT = "server.ssl.cert-path";
  String PATH_SERVER_SSL_PRIVATE_KEY = "server.ssl.private-key-path";
  String PATH_SERVER_SSL_PRIVATE_KEY_PASS = "server.ssl.private-key-pass";
  String PATH_SERVER_NETTY_ACCEPT_THREAD_COUNT = "server.netty.accept-thread-count";
  String PATH_SERVER_NETTY_IO_THREAD_COUNT = "server.netty.io-thread-count";

  // netty default property
  Integer DEFAULT_ACCEPT_THREAD_COUNT = 1;
  Integer DEFAULT_IO_THREAD_COUNT = 0;

  // app setting
  String PATH_APP_BANNER_TEXT = "app.banner.text";
  String PATH_APP_BANNER_FONT = "app.banner.font";
  String PATH_APP_THREAD_NAME = "app.thread.name";

  // full property file name
  String PATH_CONFIG_PROPERTIES = "application.properties";
  String PATH_CONFIG_YAML = "application.yml";

  // setting prefix
  String PATH_PREFIX_ROOT = "PATH_";

  // watch env
  Path SERVER_WATCHER_PATH = Paths.get(Objects.requireNonNull(Aquiver.getCurrentClassPath()));
}
