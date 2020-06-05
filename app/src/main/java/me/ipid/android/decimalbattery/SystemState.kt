package me.ipid.android.decimalbattery

import java.io.Serializable

sealed class SystemState : Serializable

class StateInitial : SystemState()
class StateInitialized(val totalMah: Int, var lastCapPercent: Int, var lastTime: Long) :
    SystemState() {

    /**
     * 计算 lastTime 与当前时间的差值是否在 interval 之内。
     */
    fun isOnTime(interval: Long): Boolean {
        val timeDiff = System.currentTimeMillis() - lastTime
        return (0 < timeDiff && timeDiff < interval)
    }
}

class StateEstimated(val totalMah: Int) : SystemState()
