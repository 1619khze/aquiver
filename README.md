# Aquiver

<p align="left">Aquiver是一个基于Java8和Netty的MVC框架</p>
<p align="left">
   <img src="https://img.shields.io/badge/JDK-8+-green.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/license-MIT-blue.svg" alt="Build Status">
   <img src="https://img.shields.io/badge/Author-1619kHz-ff69b4.svg">
 </a>
 <a target="_blank" href="https://github.com/AquiverV/aquiver">
   <img src="https://img.shields.io/badge/Copyright%20-@Aquiver-%23ff3f59.svg" alt="Downloads"/>
 </a>
 </p>

### 快速开始

通过 `Maven` 或 `Gradle` 创建项目，因为还未上传到Maven中心仓库，所以目前暂时通过clone代码进行安装

```git
git clone https://github.com/AquiverV/apex.git

cd apex

mvn clean install

git clone https://github.com/AquiverV/aquiver.git

cd aquiver

mvn clean install
```

在 `Maven` 中使用：

```xml
<dependency>
    <groupId>org.aquiver</groupId>
    <artifactId>aquiver</artifactId>
    <version>1.0.3.BETA</version>
</dependency>
```

#### 尝试

 ```java
public class Main {
  public static void main(String[] args) {
    Aquiver.of().bind(9999).get("/", ctx -> {
      Object username = ctx.param("username");
      System.out.println(paramName);
    }).start(Main.class, args);
  }
}
 ```
在浏览器打开：`http://localhost:9999/?username=1619kHz` 查看效果

## License
[MIT](https://opensource.org/licenses/MIT "MIT")

Copyright (c) 2020-present, Yi (Ever) Wang
