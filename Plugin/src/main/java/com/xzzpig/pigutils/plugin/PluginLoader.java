package com.xzzpig.pigutils.plugin;

import com.xzzpig.pigutils.annotation.NotNull;
import com.xzzpig.pigutils.annotation.Nullable;

import java.util.concurrent.atomic.AtomicReference;

public interface PluginLoader {
    /**
     * @return {@link PluginLoader}调用的优先级,越小越高
     */
    int order();

    @NotNull
    Plugin loadPlugin(@NotNull PluginManager manager, @NotNull Object obj,
                      @NotNull AtomicReference<PluginLoadResult> result);

    void unloadPlugin(Plugin plugin);

    void reloadPlugin(@NotNull Plugin plugin);

    boolean accept(@Nullable Object obj);

    boolean needSuccessNodify();

    void successNodify(PluginManager manager, Plugin plugin);

    boolean needOtherSuccessNodify();

    void othersuccessNodify(Plugin plugin);

    boolean needFailedNodify();

    void failedNodify(Object obj);

    boolean needOtherUnloadNodify();

    void otherunloadNodify(Plugin plugin);

    boolean needUnloadNodify();

    void unloadNodify(@NotNull Plugin plugin);

    boolean needWaitNodify();

    void waitNodify(@NotNull PluginManager manager, @NotNull Object obj, @Nullable Plugin plugin);

}