package me.ipid.android.decimalbattery

import android.app.Activity
import android.content.Context
import android.os.BatteryManager
import android.util.Log

private const val TAG = "BatteryInfo"

/**
 * 计算从 BatteryManager 获取的值是否是无效值。
 */
fun invalidBatteryInfo(value: Int): Boolean =
    (value == 0 || value == Int.MIN_VALUE || value == Int.MAX_VALUE)
fun invalidBatteryInfo(value: Long): Boolean =
    (value == 0L || value == Long.MIN_VALUE || value == Long.MAX_VALUE)

object BatteryInfo {
    private var initialized: Boolean = false
    private lateinit var batteryMan: BatteryManager
    private var totalMah: Int = 0

    fun initIfNeeded(act: Activity): Boolean {
        // 懒初始化：如果已经初始化，就不再初始化
        if (initialized) {
            return true
        }

        Log.d(TAG, "initIfNeeded: 获取 BatteryManager")
        batteryMan = act.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        val (capPercent, capMah) = getBatteryPropertiesUnchecked()
        if (invalidBatteryInfo(capPercent) || invalidBatteryInfo(capMah)) {
            Log.d(TAG, "initIfNeeded: 获取电量属性失败，设备不支持")
            return false
        }

        totalMah = (capMah.toDouble() / capPercent.toDouble() * 100.0).toInt()
        Log.d(TAG, "initIfNeeded: 计算出总电量为 $totalMah mAh")

        initialized = true
        return true
    }

    fun getBatteryProperties(): Triple<Int, Int, Int> {
        if (!initialized) {
            error("怎么肥四，companion object 还没初始化呢")
        }

        return getBatteryPropertiesUnchecked()
    }

    private fun getBatteryPropertiesUnchecked(): Triple<Int, Int, Int> {
        return Triple(
            batteryMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY),
            batteryMan.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER),
            totalMah
        )
    }
}