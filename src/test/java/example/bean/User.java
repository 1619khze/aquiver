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
package example.bean;

import example.config.ConfigProperty;
import org.apex.annotation.Value;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author WangYi
 * @since 2020/6/22
 */
@Singleton
public class User {
  private String name;
  private Integer age;
  private short ext;
  private String username;
  private String password;

  @Inject
  private User2 user2;

  @Inject
  private User3 user3;

  @Value("${user.home}")
  private String aa;

  @Inject
  private ConfigProperty configProperty;

  public String getUsername() {
    return username;
  }

  public User setUsername(String username) {
    this.username = username;
    return this;
  }

  public String getPassword() {
    return password;
  }

  public User setPassword(String password) {
    this.password = password;
    return this;
  }

  public void user() {
    String aa = user2.getAa();
    String username = user3.getUsername();
    String password = user3.getPassword();

    System.out.println(aa);
    System.out.println(username);
    System.out.println(password);
    System.out.println(this.aa);

    System.out.println(configProperty.toString());
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getAge() {
    return age;
  }

  public void setAge(Integer age) {
    this.age = age;
  }

  public short getExt() {
    return ext;
  }

  public void setExt(short ext) {
    this.ext = ext;
  }

  @Override
  public String toString() {
    return "User{" +
            "name='" + name + '\'' +
            ", age=" + age +
            ", ext=" + ext +
            ", user2=" + user2 +
            ", user3=" + user3 +
            ", aa='" + aa + '\'' +
            ", configProperty=" + configProperty +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            '}';
  }
}
