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
package org.application.controller;

import org.application.bean.User;
import org.aquiver.annotation.RequestMapping;
import org.aquiver.annotation.RequestMethod;
import org.aquiver.annotation.ResponseBody;
import org.aquiver.annotation.RestController;
import org.aquiver.annotation.bind.PathVariable;
import org.aquiver.annotation.bind.RequestCookies;
import org.aquiver.annotation.bind.RequestHeader;
import org.aquiver.annotation.bind.RequestParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping(value = "/controller")
public class ApplicationController {

  private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);

  @RequestMapping(value = "/requestParam", method = RequestMethod.GET)
  public String requestParam(@RequestParam String name) {
    log.info("request param:" + name);
    return "requestParam:" + name;
  }

  @ResponseBody
  @RequestMapping(value = "/requestParamJson", method = RequestMethod.GET)
  public User requestParamJson(@RequestParam String name) {
    log.info("request param:" + name);
    return new User("1", 2, (short) 3);
  }

  @RequestMapping(value = "/requestParamAlisa", method = RequestMethod.GET)
  public String requestParamAlisa(@RequestParam(value = "username") String name) {
    log.info("request param:" + name);
    return "requestParamAlisa:" + name;
  }

  @RequestMapping(value = "/requestCookies", method = RequestMethod.GET)
  public String requestCookies(@RequestCookies String name) {
    log.info("request param:" + name);
    return "requestCookies:" + name;
  }

  @RequestMapping(value = "/requestHeaders", method = RequestMethod.GET)
  public String requestHeaders(@RequestHeader(value = "Accept") String name) {
    log.info("request param:" + name);
    return "requestHeaders:" + name;
  }

  @RequestMapping(value = "/pathVariable/{name}/{code}", method = RequestMethod.GET)
  public String pathVariable(@PathVariable String name, @PathVariable String code) {
    log.info("request param:" + name + ":" + "request param:" + code);
    return "pathVariable:" + name + ":" + code;
  }
}
