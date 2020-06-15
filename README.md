# Aquiver

 <p align="left">
      <img src="https://img.shields.io/badge/JDK-11+-green.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <a target="_blank" href="https://github.com/everknwon/aquiver">
    <img src="https://img.shields.io/badge/Author-1619kHz-ff69b4.svg" alt="Downloads">
   </a>
 </a>
 <a target="_blank" href="https://github.com/everknwon/aquiver">
   <img src="https://img.shields.io/badge/Copyright%20-@Aquiver-%23ff3f59.svg" alt="Downloads">
 </a>
 </p>

> The aquifer is a java web framework based on jdk11 and netty

## Status: Beta.

### Usage example

#### StartUp HttpServer
 ```java
public class Application {
  public static void main(String[] args) {
    Aquiver.run(Main.class, args);
  }
}
```

#### Route Register
```java
@GET(value = "/get")
public String get() {
  return "/api/get";
}
```

#### Or Use @Path
```java
@Path(value = "/path",method = PathMethod.GET)
public String path(){
  return "/api/path";
}
```

#### Get Param
```java
@Path(value = "/path", method = PathMethod.GET)
public String path(@Param String paramName){
  return "/api/path";
}
```

#### Get Cookie
```java
@Path(value = "/path", method = PathMethod.GET)
public String path(@Param String cookieName){
  return "/api/path";
}
```

#### Get Body
```java
@Path(value = "/path", method = PathMethod.POST)
public String path(@Body User user){
  return "/api/path";
}
```

#### File Upload
```java
@POST(value = "/uploadFile")
public String uploadFile(@FileUpload MultipartFile file) {
  log.info("fileName:{}", file.getFileName());
  try (InputStream inputStream = file.getInputStream();
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
      sb.append(line);
    }
    System.out.println(sb.toString());
  } catch (IOException e) {
    log.error("error:", e);
  }
  return "controller/uploadFile";
}
```

#### Multi File Upload
```java
@POST(value = "/uploadFiles")
public String uploadFileS(@MultiFileUpload List<MultipartFile> files) {
  log.info("file size:{}", files.size());
  for (MultipartFile multipartFile : files) {
    log.info("fileName:{}", multipartFile.getFileName());
    try (InputStream inputStream = multipartFile.getInputStream();
           BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
    StringBuilder sb = new StringBuilder();
    String line;
    while ((line = bufferedReader.readLine()) != null) {
        sb.append(line);
    }
      System.out.println(sb.toString());
    } catch (IOException e) {
      log.error("error:", e);
    }
  }
  return "controller/uploadFiles";
}
```
## License
[MIT](https://opensource.org/licenses/MIT "MIT")

Copyright (c) 2018-present, Yi (Ever) Wang
