package org.application.bean;

import java.util.Objects;

public class User {
  private String  name;
  private Integer age;
  private short   ext;

  public User(String name, Integer age, short ext) {
    this.name = name;
    this.age  = age;
    this.ext  = ext;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User that = (User) o;
    return ext == that.ext &&
            Objects.equals(name, that.name) &&
            Objects.equals(age, that.age);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, age, ext);
  }
}
