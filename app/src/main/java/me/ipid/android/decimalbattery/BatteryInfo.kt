package me.ipid.android.decimalbattery

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.util.Log
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

private const val TAG = "BatteryInfo"
private const val STATE_FILE = "system_state.serialize"

/**
 * 计算从 BatteryManager 获取的值是否是无效值。
 */
fun invalidBatteryInfo(value: Int): Boolean =
    (value <= 0 || value == Int.MAX_VALUE)

/**
 * 计算从 BatteryManager 获取的值是否是无效值。
 */
fun invalidBatteryInfo(value: Long): Boolean =
    (value <= 0L || value == Long.MAX_VALUE)

object BatteryInfo {
    private lateinit var batteryMan: BatteryManager
    private var state: SystemState = StateInitial()

    fun init(act: Activity): Boolean {
        // 懒初始化：如果已经初始化，就不再初始化
        when (state) {
            is StateInitial -> {
                Log.d(TAG, "init: 获取 BatteryManager")
                batteryMan = act.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

                Log.d(TAG, "init: 试图从文件读取状态")
                val fileState = tryReadStateFromFile(STATE_FILE, act)
                // 如果读取成功，就将当前状态设为文件里的状态
                if (fileState != null) {
                    state = fileState
                    return true
                }

                val (capPercent, capMah) = getCapPercentAndMah()
                if (invalidBatteryInfo(capPercent) || invalidBatteryInfo(capMah)) {
                    Log.d(TAG, "init: 获取电量属性失败，设备不支持")
                    return false
                }

                state = StateInitialized(
                    estimateTotalMah(capPercent, capMah), capPercent,
                    System.currentTimeMillis()
                )
                Log.d(TAG, "init: 状态转移为 INITIALIZED")

                return true
            }
            is StateInitialized -> return true
            is StateEstimated -> return true
        }
    }

    fun getBattery(context: Context, interval: Long): Double {
        val (percent, mah) = getCapPercentAndMah()
        val result: Double

        val curState = state
        when (curState) {
            is StateInitial -> error("Unreachable")
            is StateInitialized -> {
                // 如果系统百分比值正好改变，且时间差在 updateInterval 内
                if (curState.lastCapPercent != percent && curState.isOnTime(interval)) {

                    // 此时电量百分比比较准，所以用这个时候的数据来估计电量
                    val totalMah = estimateTotalMah(percent, mah)
                    state = StateEstimated(totalMah)
                    tryWriteStateToFile(STATE_FILE, context, state)
                    Log.d(TAG, "getBattery: 状态转移为 ESTIMATED 并持久化")

                    result = mah.toDouble() / totalMah.toDouble()

                } else {
                    curState.lastTime = System.currentTimeMillis()
                    curState.lastCapPercent = percent
                    result = mah.toDouble() / curState.totalMah.toDouble()
                }
            }
            is StateEstimated -> {
                result = mah.toDouble() / curState.totalMah.toDouble()
            }
        }

        Log.d(TAG, "getBatteryText: 当前电量 $mah")
        return result
    }

    private fun estimateTotalMah(percent: Int, mah: Int): Int = mah / percent * 100

    private fun getCapPercentAndMah(): Pair<Int, Int> {
        return Pair(
            batteryMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
            batteryMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        )
    }
}