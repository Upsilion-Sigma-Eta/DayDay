package com.example.dayday

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.*
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dayday.Constants.A_MORNING_EVENT_TIME
import com.example.dayday.Constants.A_NIGHT_EVENT_TIME
import com.example.dayday.Constants.KOREA_TIMEZONE
import com.example.dayday.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkerA(context: Context, params: WorkerParameters) : Worker(context, params) {
    var str = ""

    override fun doWork(): Result {
        val mNotificationHelper = NotificationHelper(applicationContext)
        val currentMillis =
            Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA).timeInMillis

        // 알림 범위(08:00-09:00, 20:00-21:00)에 해당하는지 기준 설정
        // 2022-06-09 알람 1에서 설정한 시각으로 설정
//        var formatter = SimpleDateFormat("hh:mm:ss")
        //str = MainActivity.getInstance()?.findViewById<EditText>(R.id.alarmTime1)?.text.toString()

//        str = MainActivity.getInstance()?.findViewById<TextView>(R.id.alarmTime)?.text.toString()   //null

        // 2022-06-21 NULL이 아닌 경우에만 알람시각이 설정되도록 조정
        if (MainActivity.getInstance()?.alarmTime != null) {
            str = MainActivity.getInstance()?.alarmTime?.text.toString()
        }

        if (str.isNullOrEmpty()) {
            str = "00:00:00"
        }
        else
            str += ":00"
        var date = str

        val eventCal = NotificationHelper.getScheduledCalender(date)
        val morningNotifyMinRange = eventCal.timeInMillis
        eventCal.add(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_INTERVAL_HOUR)
        val morningNotifyMaxRange = eventCal.timeInMillis
        eventCal[Calendar.HOUR_OF_DAY] = A_NIGHT_EVENT_TIME
        val nightNotifyMinRange = eventCal.timeInMillis
        eventCal.add(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_INTERVAL_HOUR)
        val nightNotifyMaxRange = eventCal.timeInMillis

        // 현재 시각이 오전 알림 범위에 해당하는지
        val isMorningNotifyRange =
            morningNotifyMinRange <= currentMillis && currentMillis <= morningNotifyMaxRange
        // 현재 시각이 오후 알림 범위에 해당하는지
        val isNightNotifyRange =
            nightNotifyMinRange <= currentMillis && currentMillis <= nightNotifyMaxRange
        // 현재 시각이 알림 범위에 해당여부
        val isEventANotifyAvailable = isMorningNotifyRange || isNightNotifyRange

        if (isEventANotifyAvailable) {
            // 현재 시각이 알림 범위에 해당하면 알림 생성
            mNotificationHelper.createNotification(Constants.WORK_A_NAME)
        }
        else {
            // 그 외의 경우 가장 빠른 A 이벤트 예정 시각까지의 notificationDelay 계산하여 딜레이 호출
            val notificationDelay = NotificationHelper.getNotificationDelay(Constants.WORK_A_NAME)
            val workRequest = OneTimeWorkRequest.Builder(WorkerA::class.java)
                .setInitialDelay(notificationDelay, TimeUnit.MILLISECONDS)
                .build()
            WorkManager.getInstance(applicationContext).enqueue(workRequest)
        }
        return Result.success()
    }
}
