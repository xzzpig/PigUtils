package com.xzzpig.pigutils.reflect;

import com.xzzpig.pigutils.core.AsyncRunner;
import com.xzzpig.pigutils.core.AsyncRunner.RunResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;

public class MethodUtils {
    public static AsyncRunner asyncRunner = null;
    private Method method;

    public MethodUtils(Method method) {
        this.method = method;
    }

    public static String getStackMethodName(int i) {
        return new Exception().getStackTrace()[i].getMethodName();
    }

    public static Method getStackMethod(int i) {
        StackTraceElement stack = new Exception().getStackTrace()[i];
        Class<?> clazz;
        try {
            clazz = Class.forName(stack.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ClassUtils<?> cu = new ClassUtils<>(clazz);
        return cu.getMethod(stack.getMethodName());
    }

    public static Method getStackMethod(int i, Object... args) {
        StackTraceElement stack = new Exception().getStackTrace()[i];
        Class<?> clazz;
        try {
            clazz = Class.forName(stack.getClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        ClassUtils<?> cu = new ClassUtils<>(clazz);
        return cu.getMethod(stack.getMethodName(), args);
    }

    public Method getMethod() {
        return method;
    }

    public Class<?>[] getParmameters() {
        return method.getParameterTypes();
    }

    public Object invoke(Object obj, Object... parameters) {
        if (parameters == null) {
            parameters = new Object[0];
        }
        if (parameters.length != getParmameters().length) {
            parameters = Arrays.copyOf(parameters, getParmameters().length);
        }
        boolean access = method.isAccessible();
        method.setAccessible(true);
        Object result = null;
        try {
            result = method.invoke(obj, parameters);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        }
        method.setAccessible(access);
        return result;
    }

    public MethodUtils invokeAsync(Object obj, Consumer<RunResult<Object>> callback, int timeout,
                                   Object... parameters) {
        if (asyncRunner == null) {
            asyncRunner = new AsyncRunner(5, true);
        }
        asyncRunner.run(()->invoke(obj, parameters), callback, timeout);
        // new AsyncRunner<>(() -> invoke(obj, parameters), callback,
        // timeout).run();
        return this;
    }
}
