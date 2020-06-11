package me.ipid.android.decimalbattery

import android.content.Context
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable

private const val TAG = "SystemState"

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

fun tryWriteStateToFile(file: String, context: Context, state: SystemState): Boolean {
    try {
        ObjectOutputStream(
            context.openFileOutput(
                file, Context.MODE_PRIVATE
            )
        ).use {
            it.writeObject(state)
        }
    } catch (e: Exception) {
        Log.e(TAG, "tryWriteStateToFile: state 持久化失败", e)
        return false
    }

    return true
}

fun tryReadStateFromFile(file: String, context: Context): SystemState? {
    var state: SystemState? = null

    try {
        ObjectInputStream(context.openFileInput(file)).use {
            state = it.readObject() as SystemState
        }
    } catch (e: Exception) {
        Log.e(TAG, "readStateFromFile: 从文件读取 state 失败", e)
    }

    return state
}