package mapper;

import java.util.function.Function;

public class FieldInfo {
  private String name;
  private Class<?> type;
  private ReflectionUtils.SetterClass setterFunc;
  private Function getterFunc;

  public FieldInfo(){}

  public FieldInfo(String name, Class<?> type, ReflectionUtils.SetterClass setterFunc, Function getterFunc) {
    this.name = name;
    this.type = type;
    this.setterFunc = setterFunc;
    this.getterFunc = getterFunc;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Class<?> getType() {
    return type;
  }

  public void setType(Class<?> type) {
    this.type = type;
  }

  public ReflectionUtils.SetterClass getSetterFunc() {
    return setterFunc;
  }

  public void setSetterFunc(ReflectionUtils.SetterClass setterFunc) {
    this.setterFunc = setterFunc;
  }

  public Function getGetterFunc() {
    return getterFunc;
  }

  public void setGetterFunc(Function getterFunc) {
    this.getterFunc = getterFunc;
  }
}
