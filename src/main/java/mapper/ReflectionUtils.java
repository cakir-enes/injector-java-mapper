package mapper;

import java.lang.invoke.*;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO Refactor.
@SuppressWarnings("unchecked")
public final class ReflectionUtils {
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final Map<String, Map<String, FieldInfo>> moduleMap = new HashMap<>();
  private static final Map<String, Map<Class<?>, Object[]>> moduleEnumsMap = new HashMap<>();

  private static Map<String, FieldInfo> fieldMap;
  private static Map<Class<?>, Object[]> enumsMap;
//  private static Logger

  private static String moduleName = null;

  public static void discoverObject(Class<?> clazz, Object obj, String moduleNameParam) {
    moduleName = moduleNameParam;
    fieldMap = new LinkedHashMap<>();
    enumsMap = new HashMap<>();
    discoverObject(clazz, obj, moduleName, new LinkedList<>());
    moduleMap.put(moduleNameParam, fieldMap);
    moduleEnumsMap.put(moduleNameParam, enumsMap);
  }

  public static Object parameterGet(String moduleName, Object obj, String fullPath) throws IllegalArgumentException {
//    logger.debug("GET CALLED {}", fullPath);
    if (!fullPath.contains(".") && fullPath.equals(moduleName)) {
      return obj;
    }
    try {
      return moduleMap.get(moduleName).get(fullPath).getGetterFunc().apply(obj);
    } catch (NullPointerException e) {
//      logger.error("Cant find {}", fullPath);
      throw new IllegalArgumentException("Can't find " + fullPath);
    }
  }

  public static List<Object> parameterMultiGet(String moduleName, Object obj, List<String> paths) {
    List<Object> vals = paths.parallelStream()
                            .map(fullPath -> parameterGet(moduleName, obj, fullPath))
                            .collect(Collectors.toList());
    return vals;
  }
// TODO handle errors
  public static void parameterSet(String moduleName, Object obj, String fullPath, Object value) throws IllegalArgumentException {
//    logger.debug("SET CALLED {}", fullPath);
    String path = fullPath.substring(0, fullPath.lastIndexOf("."));
    Object target = parameterGet(moduleName, obj, path);
    FieldInfo fieldInfo = fieldMap.get(fullPath);
    SetterClass setterFunc = fieldInfo.getSetterFunc();
    Class<?> type = fieldInfo.getType();
    if (type.equals(int.class) || type.equals(Integer.class)) {
      ((IntSetter) setterFunc).accept(target, Integer.parseInt(value.toString()));
    } else if (type.equals(double.class)) {
      ((DoubleSetter) setterFunc).accept(target, Double.parseDouble(value.toString()));
    } else if (type.equals(float.class)) {
      ((FloatSetter) setterFunc).accept(target,  Float.parseFloat(value.toString()));
    } else if (type.equals(byte.class)) {
      ((ByteSetter) setterFunc).accept(target, Byte.parseByte(value.toString()));
    } else if (type.equals(long.class)) {
      ((LongSetter) setterFunc).accept(target, Long.parseLong(value.toString()));
    } else if (type.equals(Object.class)) {
      ((ObjectSetter) setterFunc).accept(target, value);
    }
  }

  // FIXME If the objects array field changed to have more elements, discover object has to be called again in order to create new getter/setters for the new elems!
  private static void discoverObject(Class<?> clazz, Object obj, String parent, LinkedList<Function> getterFuncs) {
    try {
      for (Method method : clazz.getDeclaredMethods()) {
        String name = method.getName();
        method.setAccessible(true);
        TypeOfMethod typeOfMethod = TypeOfMethod.OTHER;
        if (name.startsWith("get")) {
          typeOfMethod = TypeOfMethod.GET;
        }
        if (name.startsWith("set")) {
          typeOfMethod = TypeOfMethod.SET;
        }
        switch (typeOfMethod) {
          case OTHER:
            break;
          case GET:
            Class<?> returnType = method.getReturnType();
            if (name.equals("getInstance") || returnType.equals(void.class)) continue;
            if (method.getParameterTypes().length != 0) continue; // getXYZ(void) methods is our concern.
            if (isPrimitive(returnType)) {
              String path = parent + "." + name.substring(3);
              Function getterFunc = createGetterFunc(method);
              LinkedList<Function> getters = new LinkedList<>(getterFuncs);
              getters.add(getterFunc);
              FieldInfo fieldInfo = fieldMap.getOrDefault(path, new FieldInfo(path, null, null, null));
              fieldInfo.setType(returnType);
              fieldInfo.setGetterFunc(getters.stream().reduce(Function::andThen).get());
              fieldMap.put(path, fieldInfo);

            } else if (returnType.isArray()) {
              Class<?> compType = returnType.getComponentType();

              if (obj == null) {
                continue;
              }

              Object arr = method.invoke(obj);

              if (arr == null) {
                continue;
              }

              String arrPath = parent + "." + name.substring(3);
              Function getterFunc = createGetterFunc(method);
              LinkedList<Function> getters = new LinkedList<>(getterFuncs);
              getters.add(getterFunc);
              FieldInfo fieldInfo = fieldMap.getOrDefault(arrPath, new FieldInfo(arrPath, null, null, null));
              fieldInfo.setType(returnType);
              fieldInfo.setGetterFunc(getters.stream().reduce(Function::andThen).get());
              fieldMap.put(arrPath, fieldInfo);
              for (int i = 0; i < Array.getLength(arr); i++) {
                String path = arrPath + "[" + i + "]";

                int finalI = i;
                Function ge = (arrObj) -> Array.get(arrObj, finalI);
                LinkedList<Function> tempGetters = new LinkedList<>(getters);
                tempGetters.add(ge);
                fieldInfo = fieldMap.getOrDefault(path, new FieldInfo(path, null, null, null));
                fieldInfo.setType(returnType);
                fieldInfo.setGetterFunc(tempGetters.stream().reduce(Function::andThen).get());
                fieldMap.put(path, fieldInfo);
                // TODO: Is this really a problem? cant set a[i] to a new val with code.
//                                SetterFunc<Object, Object> setterFunc = (arrObj, val) -> Array.set(arrObj, finalI, val);
//                                fieldsToSetters.put(path, setterFunc);
                if (!isPrimitive(compType)) {
                  Object toDiscover = Array.get(arr, i);
                  if (toDiscover == null) continue;
                  discoverObject(compType, toDiscover, path, tempGetters);
                }
              }

            } else {
              LinkedList<Function> getters = new LinkedList<>(getterFuncs);
              getters.add(createGetterFunc(method));
              String path = parent + "." + name.substring(3);
              FieldInfo fieldInfo = fieldMap.getOrDefault(path, new FieldInfo(path, null, null, null));
              fieldInfo.setType(returnType);
              fieldInfo.setGetterFunc(getters.stream().reduce(Function::andThen).get());
              fieldMap.put(path, fieldInfo);
              Object toDiscover = method.invoke(obj);
              if (toDiscover == null) continue;
              discoverObject(returnType, toDiscover, path, getters);
            }
            break;
          case SET:
            String path = parent + "." + name.substring(3);
            Class<?> paramType = method.getParameterTypes()[0];
            FieldInfo fieldInfo = fieldMap.getOrDefault(path, new FieldInfo(path, paramType, null, null));
            if (paramType.equals(int.class)) {
              fieldInfo.setSetterFunc(createIntSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            } else if (paramType.equals(long.class)) {
              fieldInfo.setSetterFunc(createLongSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            } else if (paramType.equals(float.class)) {
              fieldInfo.setSetterFunc(createFloatSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            } else if (paramType.equals(double.class)) {
              fieldInfo.setSetterFunc(createDoubleSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            } else if (paramType.equals(byte.class)) {
              fieldInfo.setSetterFunc(createByteSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            } else {
              fieldInfo.setType(Object.class);
              fieldInfo.setSetterFunc(createObjectSetterFunc(method));
              fieldMap.put(path, fieldInfo);
            }
            break;
        }
      }
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
  }

  private enum TypeOfMethod {
    GET, SET, OTHER
  }

//    public static Map<String, Function> findGetters(Class<?> clazz) {
//        final Map<String, Function> getters = Stream.of(clazz.getDeclaredMethods())
//                                                .filter(method -> method.getName().startsWith("get"))
//                                                .map(method -> {
//                                                    final Class<?> returnType = method.getReturnType();
//                                                   return new AbstractMap.SimpleEntry<String, Function>(method.getName().substring(3), createGetterFunc(method));
//                                                })
//                                                .collect(Collectors.toMap(entry -> entry.getKey(), e -> e.getValue()));
//        return getters;
//    }

  private static Function createGetterFunc(Method method) {
    Function getterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "apply",
          MethodType.methodType(Function.class),
          h.type().generic(), h, h.type());

      getterFunc = ((Function) cs.getTarget().invoke());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return getterFunc;
  }

  private static IntSetter createIntSetterFunc(Method method) {
    IntSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(IntSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((IntSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }

  // TODO: More generic way to generate lambdafuncs ?
//    private static <T extends SetterClass> T createObjectSetterFunc(Method method) {
//        T setterFunc = null;
//        try {
//            final MethodHandle h = LOOKUP.unreflect(method);
//            final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
//                    "accept",
//                    MethodType.methodType(ObjectSetter.class),
//                    h.type().erase(), h, h.type());
//
//            setterFunc = ((T) cs.getTarget().invokeExact());
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
//        return setterFunc;
//    }

  private static ObjectSetter createObjectSetterFunc(Method method) {
    ObjectSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(ObjectSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((ObjectSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }

  private static LongSetter createLongSetterFunc(Method method) {
    LongSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(LongSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((LongSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }

  private static DoubleSetter createDoubleSetterFunc(Method method) {
    DoubleSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(DoubleSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((DoubleSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }

  private static FloatSetter createFloatSetterFunc(Method method) {
    FloatSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(FloatSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((FloatSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }

  private static ByteSetter createByteSetterFunc(Method method) {
    ByteSetter setterFunc = null;
    try {
      final MethodHandle h = LOOKUP.unreflect(method);
      final CallSite cs = LambdaMetafactory.metafactory(LOOKUP,
          "accept",
          MethodType.methodType(ByteSetter.class),
          h.type().erase(), h, h.type());

      setterFunc = ((ByteSetter) cs.getTarget().invokeExact());
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return setterFunc;
  }


  public interface SetterClass {
  }

  @FunctionalInterface
  private interface ObjectSetter extends SetterClass {
    void accept(Object target, Object val);
  }

  @FunctionalInterface
  private interface IntSetter extends SetterClass {
    void accept(Object obj, int val);
  }

  @FunctionalInterface
  private interface LongSetter extends SetterClass {
    void accept(Object target, long val);
  }

  @FunctionalInterface
  private interface ByteSetter extends SetterClass {
    void accept(Object target, byte val);
  }

  @FunctionalInterface
  private interface DoubleSetter extends SetterClass {
    void accept(Object target, double val);
  }

  @FunctionalInterface
  private interface FloatSetter extends SetterClass {
    void accept(Object target, float val);
  }

  public static boolean isPrimitive(Class<?> clazz) {
    Stream<Class> wrapperClasses = Stream.of(Integer.class, Long.class, Double.class, Float.class, String.class, Character.class, Boolean.class);
    if (clazz.isEnum()) {
      enumsMap.putIfAbsent(clazz, clazz.getEnumConstants());
    }
    return clazz.isPrimitive() || clazz.isEnum() || wrapperClasses.anyMatch(clazz::equals);
  }


  public static Map<Class<?>, Object[]> getEnumsMap(String moduleName) {
    return moduleEnumsMap.get(moduleName);
  }


  public static Map<String, FieldInfo> getFieldMap(String moduleName) {
    return moduleMap.get(moduleName);
  }
}
