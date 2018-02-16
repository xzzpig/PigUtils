package com.xzzpig.pigutils.event;

import com.xzzpig.pigutils.core.IData;
import com.xzzpig.pigutils.core.MapData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

/**
 * 作为EventRunner的载体
 *
 * @author xzzpig
 */
public class EventBus {

    private final List<EventRunner<?>> runners = new ArrayList<>();

    /**
     * callEvent(e,null)
     *
     * @see EventBus#callEvent(Event, EventTunnel)
     */
    public void callEvent(Event e) {
        callEvent(e, null);
    }

    /**
     * 将e作为参数,按一定顺序调用this注册的EventRunner 当
     * {@link EventRunner#getEventTunnel()}.equal(tunnel) 或 tunnel==null 且
     * {@link EventRunner#getLimits()}的每个元素test下来都为true时 EventRunner才会被执行
     *
     * @param e      事件
     * @param tunnel 事件通道,null则为所有通道
     * @return 最后一个调用的 {@link EventRunner#run(Event)} 的返回值
     */
    public void callEvent(Event e, EventTunnel tunnel) {
        e.runners = new ArrayList<>(runners);
        e.eventbus = this;
        run:
        for (EventRunner<?> r : e.runners) {
            if (e.isCanceled() && !r.ignoreCanceled())
                continue;
            if (tunnel != null && !r.getEventTunnel().equals(tunnel)) {
                continue;
            }
            if (r.getLimits() != null)
                for (Predicate<Event> p : r.getLimits())
                    if (!p.test(e))
                        continue run;
            @SuppressWarnings("unchecked")
            EventRunner<Event> r2 = (EventRunner<Event>) r;
            try {
                r2.run(e);
            } catch (ClassCastException e2) {
            }
        }
    }

    /**
     * 调用regListener(c.newInstance()0
     *
     * @param c 此类需要有无参数的构造函数
     * @return this
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @see EventBus#regListener(Listener)
     */
    public EventBus regListener(Class<? extends Listener> c) throws InstantiationException, IllegalAccessException {
        return regListener(c.newInstance());
    }

    /**
     * 将listener中含@{@link EventHandler}注解的方法解析为EventRunner 并使用
     * {@link EventBus#regRunner(EventRunner)}方法注册
     *
     * @return this
     * @see EventBus#regRunner(EventRunner)
     * @see EventHandler
     */
    public EventBus regListener(Listener listener) {
        for (Method method : listener.getClass().getDeclaredMethods()) {
            EventHandler handler = method.getDeclaredAnnotation(EventHandler.class);
            if (handler == null)
                continue;
            method.setAccessible(true);
            regRunner(new EventRunner<Event>() {

                @Override
                public boolean canRun(Event e) {
                    Class<?> target = method.getParameterTypes()[0];
                    return target.isAssignableFrom(e.getClass());
                }

                @Override
                public EventTunnel getEventTunnel() {
                    return handler.tunnel().equalsIgnoreCase("default") ? EventTunnel.defaultTunnel
                            : new EventTunnel(handler.tunnel());
                }

                @Override
                public IData getInfo() {
                    return new MapData(new HashMap<>()).set("listener", listener.toString()).set("method", method.getName())
                            .set("class", listener.getClass().getName());
                }

                @Override
                public int getMinorRunLevel() {
                    return handler.minorLevel();
                }

                @Override
                public EventRunLevel getRunLevel() {
                    return handler.mainLevel();
                }

                @Override
                public boolean ignoreCanceled() {
                    return handler.ignoreCanceled();
                }

                @Override
                public void run(Event event) {
                    try {
                        method.invoke(listener, event);
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        return this;
    }

    /**
     * 调用 regRunner(c.newInstance())
     *
     * @param c 此类需要有无参数的构造函数
     * @return this
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @see EventBus#regRunner(EventRunner)
     */
    public EventBus regRunner(Class<? extends EventRunner<?>> c) throws InstantiationException, IllegalAccessException {
        regRunner(c.newInstance());
        return this;
    }

    /**
     * 将runner注册到本EventBus
     *
     * @return this
     */
    public EventBus regRunner(EventRunner<?> runner) {
        runners.add(runner);
        runners.sort((r1, r2)->{
            if (r1.getRunLevel().ordinal() > r2.getRunLevel().ordinal()) {
                return 1;
            } else if (r1.getRunLevel().ordinal() < r2.getRunLevel().ordinal()) {
                return -1;
            } else {
                return Integer.compare(r1.getMinorRunLevel(), r2.getMinorRunLevel());
            }
        });
        return this;
    }

    /**
     * 解除注册所有该Listener中可被解析为 {@link EventRunner} 的方法
     *
     * @return this
     */
    public EventBus unregListener(Class<Listener> c) {
        unregRunner(r->r.getInfo() != null && r.getInfo().get("class", String.class, "").equalsIgnoreCase(c.getName()));
        return this;
    }

    /**
     * 解除注册所有该Listener中可被解析为 {@link EventRunner} 的方法
     *
     * @return this
     */
    public EventBus unregListener(Listener listener) {
        unregRunner(r->r.getInfo() != null && r.getInfo().get("listener", String.class, "").equalsIgnoreCase(listener.toString()));
        return this;
    }

    /**
     * 解除注册所有类为c或继承于c的 {@link EventRunner}
     *
     * @return this
     */
    public EventBus unregRunner(Class<? extends EventRunner<?>> c) {
        unregRunner(r->c.isAssignableFrom(r.getClass()));
        return this;
    }

    /**
     * 解除注册 {@link EventRunner}: 遍历所有注册的 {@link EventRunner}并使用p进行测试
     * 返回true时这解除注册该 {@link EventRunner}
     *
     * @return this
     */
    public EventBus unregRunner(Predicate<EventRunner<?>> p) {
        List<EventRunner<?>> removeList = new ArrayList<>();
        for (EventRunner<?> r : runners) {
            try {
                if (p.test(r))
                    removeList.add(r);
            } catch (Exception e) {
            }
        }
        runners.removeAll(removeList);
        return this;
    }
}
