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
package example.route;

import example.bean.User;
import io.netty.channel.ChannelHandlerContext;
import org.apex.ApexContext;
import org.aquiver.ModelAndView;
import org.aquiver.RequestContext;
import org.aquiver.mvc.annotation.*;
import org.aquiver.mvc.annotation.bind.*;
import org.aquiver.mvc.router.multipart.MultipartFile;
import org.aquiver.mvc.router.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path(value = "/controller")
public class ApiRoute {

  private static final Logger log = LoggerFactory.getLogger(ApiRoute.class);

  @Inject
  private User user;

  @Path(value = "/requestParam", method = HttpMethod.GET)
  public String requestParam(@Param String name) {
    log.info("request param:" + name);
    return "requestParam:" + name;
  }

  @JSON
  @Path(value = "/requestParamJson", method = HttpMethod.GET)
  public User requestParamJson(@Param String name) {
    log.info("request param:" + name);
    user.setUsername("hello");
    user.setPassword("world");
    user.setAge(1);
    user.setExt((short) 1);
    user.setName(name);
    user.user();

    ApexContext apexContext = ApexContext.of();
    User bean = apexContext.getBean(User.class);
    bean.user();
    return user;
  }

  @Path(value = "/requestParamAlisa", method = HttpMethod.GET)
  public String requestParamAlisa(@Param(value = "username") String name) {
    log.info("request param:" + name);
    return "requestParamAlisa:" + name;
  }

  @Path(value = "/requestCookies", method = HttpMethod.GET)
  public String requestCookies(@Cookies String name) {
    log.info("request param:" + name);
    return "requestCookies:" + name;
  }

  @Path(value = "/requestHeaders", method = HttpMethod.GET)
  public String requestHeaders(@Header(value = "Accept") String name) {
    log.info("request param:" + name);
    return "requestHeaders:" + name;
  }

  @Path(value = "/pathVariable/{name}/{code}", method = HttpMethod.GET)
  public String pathVariable(@PathVar String name, @PathVar String code) {
    log.info("request param:" + name + ":" + "request param:" + code);
    return "pathVariable:" + name + ":" + code;
  }

  @Path(value = "/postBody", method = HttpMethod.POST)
  public String postBody(@Body User user) {
    log.info("post body param:" + user);
    return "post body:" + user;
  }

  @View
  @GET(value = "/getHtml")
  public String getHtml() {
    return "/index.html";
  }

  @View
  @GET(value = "/getPeb")
  public String getPeb() {
    return "/templates/index.peb";
  }

  @View
  @GET(value = "/get")
  public String get() {
    return "/index";
  }

  @View
  @GET(value = "/modelAndView")
  public ModelAndView modelAndView() {
    ModelAndView modelAndView = new ModelAndView();

    Map<String, Object> paramMap = new HashMap<>();
    paramMap.put("name", "Mitchell");

    modelAndView.htmlPath("index.html");
    modelAndView.params(paramMap);
    return modelAndView;
  }

  @GET(value = "/void")
  public void testVoid() {
    log.info("void test");
  }

  @GET(value = "/redirectaaaa")
  public String redirect() {
    return "redirect:http://www.baidu.com";
  }

  @GET(value = "/forwardaaaa")
  public String forward() {
    return "forward:http://localhost:9999/controller/void";
  }

  @GET(value = "/doc.xml")
  public Document document() {
    DocumentBuilder builder;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    }
    catch(ParserConfigurationException e) {
      throw new IllegalStateException(e);
    }
    Document doc = builder.newDocument();
    Element users = doc.createElement("users");
    users.setAttribute("department", "XX");
    doc.appendChild(users);

    Element user = doc.createElement("user");
    user.setAttribute("id", "3");
    user.setAttribute("name", "张三");
    users.appendChild(user);

    user = doc.createElement("user");
    user.setAttribute("id", "4");
    user.setAttribute("name", "李四");
    users.appendChild(user);

    return doc;
  }

  @GET(value = "xml")
  public String xml() {
    return "xml:<node/>";
  }

  @GET(value = "/session")
  public void session(Session session) {
    String id = session.getId();
    System.out.println(id);
    session.setAttribute("loginName", "WangYi");
  }

  @GET(value = "/session2")
  public void session2(Session session) {
    Object loginName = session.getAttribute("loginName");
    System.out.println(loginName);
  }

  @POST(value = "/uploadFile")
  public String uploadFile(@FileUpload MultipartFile file) throws IOException {
    log.info("fileName:{}", file.fileName());
    System.out.println(file.readFileContent());
    return "controller/uploadFile";
  }

  @POST(value = "/uploadFiles")
  public void uploadFileS(@MultiFileUpload List<MultipartFile> files) throws IOException {
    log.info("file size:{}", files.size());
    for (MultipartFile multipartFile : files) {
      log.info("fileName:{}", multipartFile.fileName());
      System.out.println(multipartFile.readFileContent());
    }
  }

  @GET(value = "/downloadFile")
  public void downloadFile(MultipartFile multipartFile) {
    multipartFile.download("C:\\Users\\ever\\Desktop\\aa.png");
  }

  @GET(value = "/downloadFilea")
  public void downloadFilea(ChannelHandlerContext multipartFile) {
    System.out.println(multipartFile);
  }

  @GET(value = "/exception")
  public void exception() {
    throw new NullPointerException("aaa");
  }

  @GET(value = "/rc")
  public void requestContext(RequestContext requestContext) {
    System.out.println(requestContext.request().uri());
  }
}
