package me.ipid.android.decimalbattery

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import kotlinx.android.synthetic.main.activity_main.*

private const val TAG = "MainActivity"

/**
 * 更新电量的间隔时间（ms）。
 */
const val UPDATE_INTERVAL: Long = 500

class MainActivity : AppCompatActivity() {

    // 缓存的全局 handler，用于定时更新电量
    val handler = Handler()

    // 全局唯一 Runnable，用于更新电量
    val updater = this.BatteryUpdater()

    /**
     * 更新电量值，并在指定时间过后再次递归更新。
     */
    inner class BatteryUpdater: Runnable {
        override fun run() {
            // 尝试懒初始化，如果不成功就弹框
            val initSucceed = BatteryInfo.initIfNeeded(this@MainActivity)
            if (!initSucceed) {
                mustExitDialog(this@MainActivity)
                return
            }

            // 更新电量文本
            text_battery.text = getBatteryText()

            // postDelayed 只执行一次，因此需要递归调用
            handler.postDelayed(this, UPDATE_INTERVAL)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onStart() {
        super.onStart()

        // 在 resume 时立刻更新电量
        Log.d(TAG, "onStart: 回到前台，开始更新电量")
        updater.run()
    }

    override fun onStop() {
        super.onStop()

        // 在切换到后台后停止更新电量
        Log.d(TAG, "onStop: 切入后台，删除电量更新 updater")
        handler.removeCallbacks(updater)
    }

    /**
     * 得出电量百分比的文本，用于显示
     */
    private fun getBatteryText(): String {
        val (_, capMah, totalMah) = BatteryInfo.getBatteryProperties()

        val batteryStr = String.format("%.2f%%", capMah.toDouble() / totalMah.toDouble() * 100.0)
        Log.d(TAG, "getBatteryText: 当前电量 $capMah，总电量 $totalMah，结果为 $batteryStr")
        return batteryStr
    }

}