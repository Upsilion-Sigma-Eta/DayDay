package com.example.dayday

import android.content.Context
import android.widget.EditText
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.dayday.Constants.B_MORNING_EVENT_TIME
import com.example.dayday.Constants.B_NIGHT_EVENT_TIME
import com.example.dayday.Constants.KOREA_TIMEZONE
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class WorkerB(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val mNotificationHelper = NotificationHelper(applicationContext)
        val currentMillis =
            Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA).timeInMillis

        // 2022-06-09 알람 2에서 설정한 시각으로 설정
//        var formatter = SimpleDateFormat("hh:mm:ss")
//        var str = MainActivity.getInstance()?.findViewById<EditText>(R.id.alarmTime2)?.text.toString()
//        if (str.isNullOrEmpty()) {
//            str = "00:00:00"
//        }
//        var date = str
//
//        // 알림 범위(08:00-09:00, 20:00-21:00)에 해당하는지 기준 설정
//        val eventCal = NotificationHelper.getScheduledCalender(date)
//        val morningNotifyMinRange = eventCal.timeInMillis
//        eventCal.add(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_INTERVAL_HOUR)
//        val morningNotifyMaxRange = eventCal.timeInMillis
//        eventCal[Calendar.HOUR_OF_DAY] = B_NIGHT_EVENT_TIME
//        val nightNotifyMinRange = eventCal.timeInMillis
//        eventCal.add(Calendar.HOUR_OF_DAY, Constants.NOTIFICATION_INTERVAL_HOUR)
//        val nightNotifyMaxRange = eventCal.timeInMillis
//
//        // 현재 시각이 알림 범위에 해당하면 알림 생성
//        // 그 외의 경우 가장 B 이벤트 예정 시각까지의 notificationDelay 계산하여 딜레이 호출
//        val isMorningNotifyRange =
//            morningNotifyMinRange <= currentMillis && currentMillis <= morningNotifyMaxRange
//        val isNightNotifyRange =
//            nightNotifyMinRange <= currentMillis && currentMillis <= nightNotifyMaxRange
//        val isEventANotifyAvailable = isMorningNotifyRange || isNightNotifyRange
//
//        if (isEventANotifyAvailable) {
//            mNotificationHelper.createNotification(Constants.WORK_B_NAME)
//        }
//        else {
//            val notificationDelay = NotificationHelper.getNotificationDelay(Constants.WORK_B_NAME)
//            val workRequest = OneTimeWorkRequest.Builder(WorkerA::class.java)
//                .setInitialDelay(notificationDelay, TimeUnit.MILLISECONDS)
//                .build()
//            WorkManager.getInstance(applicationContext).enqueue(workRequest)
//        }
        return Result.success()
    }
}
