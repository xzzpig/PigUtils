package com.xzzpig.pigutils.data

interface Observable {
    fun onBind(container: Any, key: Any) {}

    fun onUnbind(container: Any, key: Any) {}
}