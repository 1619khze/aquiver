# Aquiver

<p align="left">The aquifer is a java web framework based on jdk11 and netty</p>
<p align="left">
   <img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Author-1619kHz-ff69b4.svg">
 </a>
 <a target="_blank" href="https://github.com/AquiverV/aquiver">
   <img src="https://img.shields.io/badge/Copyright%20-@Aquiver-%23ff3f59.svg" alt="Downloads"/>
 </a>
 </p>

### Quick Start

Create a basic `Maven` or `Gradle` project.

> Do not create a `webapp` project, Aquiver does not require much trouble.

```git
git clone https://github.com/AquiverV/apex.git

cd apex

mvn clean install

git clone https://github.com/AquiverV/aquiver.git

cd aquiver

mvn clean install
```

Run with `Maven`:

```xml
<dependency>
    <groupId>org.aquiver</groupId>
    <artifactId>aquiver</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

#### Create Http Server

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
    //todo
}
```

#### Or Use @Path
```java
@Path(value = "/path",method = PathMethod.GET)
public String path(){
    //todo
}
```

#### Get Param
```java
@Path(value = "/path", method = PathMethod.GET)
public String path(@Param String paramName){
    //todo
}
```

#### Get Cookie
```java
@Path(value = "/path", method = PathMethod.GET)
public String path(@Param String cookieName){
    //todo
}
```

#### Get Body
```java
@Path(value = "/path", method = PathMethod.POST)
public String path(@Body User user){
    //todo
}
```

#### File Upload
```java
@POST(value = "/uploadFile")
public void uploadFile(@FileUpload MultipartFile file) {
  log.info("fileName:{}", file.getFileName());
  try {
    System.out.println(multipartFile.readFileContent());
  } catch (IOException e) {
    e.printStackTrace();
  }
}
```

#### Multi File Upload
```java
@POST(value = "/uploadFiles")
public void uploadFileS(@MultiFileUpload List<MultipartFile> files) {
  log.info("file size:{}", files.size());
  for (MultipartFile multipartFile : files) {
    log.info("fileName:{}", multipartFile.getFileName());
    try {
      System.out.println(multipartFile.readFileContent());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
```
## License
[MIT](https://opensource.org/licenses/MIT "MIT")

Copyright (c) 2020-present, Yi (Ever) Wang
