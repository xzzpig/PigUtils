package com.xzzpig.pigutils.data;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * 将2个Object绑定起来
 *
 * @author xzzpig
 */
public class ObjectBinder {

    private static Map<String, ObjectBinder> binderMap;
    private List<Object[]> bindList;

    public ObjectBinder() {
        bindList = new ArrayList<>();
    }

    /**
     * 获取指定名称的 {@link ObjectBinder}
     */
    public static @NotNull ObjectBinder getBinder(@Nullable String name) {
        if (name == null)
            name = "Default";
        if (binderMap == null)
            binderMap = new Hashtable<>();
        if (!binderMap.containsKey(name))
            binderMap.put(name, new ObjectBinder());
        return binderMap.get(name);
    }

    public ObjectBinder bind(@NotNull Object obj1, @NotNull Object obj2) {
        reBind(obj1, obj2);
        return this;
    }

    public ObjectBinder firstBind(@NotNull Object obj1, @NotNull Object obj2) {
        bindList.add(new Object[]{obj1, obj2});
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T getBind(@NotNull Object obj, @NotNull Class<T> targetClass) {
        synchronized (bindList) {
            for (Object[] objects : bindList) {
                if (objects[0].equals(obj) && targetClass.isInstance(objects[1]))
                    return (T) objects[1];
                else if (objects[1].equals(obj) && targetClass.isInstance(objects[0]))
                    return (T) objects[0];
            }
        }
        return null;
    }

    public ObjectBinder reBind(@NotNull Object obj1, @NotNull Object obj2) {
        unBind(obj1, obj2.getClass());
        firstBind(obj1, obj2);
        return this;
    }

    public void unBind(Object obj) {
        bindList.removeIf(objs->objs[0].equals(obj) || objs[1].equals(obj));
    }

    public void unBind(Object obj, Class<?> targetClass) {
        bindList.removeIf(objs->(objs[0].equals(obj) && targetClass.isInstance(objs[1]))
                || (objs[1].equals(obj) && targetClass.isInstance(objs[0])));
    }

}
