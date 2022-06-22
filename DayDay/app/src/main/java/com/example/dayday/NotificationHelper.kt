package com.example.dayday


import android.app.Notification
import com.example.dayday.PreferenceHelper.getBoolean
import android.content.Intent
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import android.app.PendingIntent
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.ExistingPeriodicWorkPolicy
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import android.app.NotificationChannel
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import androidx.core.content.ContextCompat.getSystemService
import java.lang.NullPointerException
import java.util.*
import java.util.concurrent.TimeUnit

import com.example.dayday.Constants.A_MORNING_EVENT_TIME
import com.example.dayday.Constants.A_NIGHT_EVENT_TIME
import com.example.dayday.Constants.B_MORNING_EVENT_TIME
import com.example.dayday.Constants.B_NIGHT_EVENT_TIME
import com.example.dayday.Constants.KOREA_TIMEZONE
import com.example.dayday.Constants.NOTIFICATION_CHANNEL_ID
import com.example.dayday.Constants.WORK_A_NAME
import com.example.dayday.Constants.WORK_B_NAME
import java.text.SimpleDateFormat
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlinx.android.synthetic.main.activity_main.*

class NotificationHelper internal constructor(private val mContext: Context) {
    fun createNotification(workName: String) {
        // 클릭 시 MainActivity 호출
        val intent = Intent(mContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) // 대기열에 이미 있다면 MainActivity가 아닌 앱 활성화
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        val notificationManager =
            mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Notificatoin을 이루는 공통 부분 정의
        val notificationBuilder: NotificationCompat.Builder =
            NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.smile) // 기본 제공되는 이미지
            .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
            .setAutoCancel(true) // 클릭 시 Notification 제거

        // 매개변수가 WorkerA라면
        if (workName == WORK_A_NAME) {
            // Notification 클릭 시 동작할 Intent 입력, 중복 방지를 위해 FLAG_CANCEL_CURRENT로 설정, CODE를 다르게하면 Notification 개별 생성
            // Code가 같으면 같은 알림으로 인식하여 갱신작업 진행
            // 2022-05-13 변경 사항 : S+ 버전 이상에서는 FLAG_MUTABLE 또는 FLAG_IMMUTABLE을 붙여줘야 함.
            val pendingIntent = PendingIntent.getActivity(
                mContext,
                WORK_A_NOTIFICATION_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            // 2022-06-09 참고용 코드 제거
            // Notification 제목, 컨텐츠 설정
//            notificationBuilder.setContentTitle("WorkerA Notification")
//                .setContentText("set a Notification contents")
//                .setContentIntent(pendingIntent)
//            notificationManager?.notify(WORK_A_NOTIFICATION_CODE, notificationBuilder.build())

            // 2022-06-09 MainActivity에서 알림을 생성하는 코드를 가져옴
            val weather_iter = MainActivity.getInstance()?.model?.weather_data?.value?.iterator()
            val notification_list = mutableListOf<Notification>()
            var i = 0
            for (i in 0 until (MainActivity.getInstance()?.model?.schedule_list?.value!!.size)) {

                var schedule = MainActivity.getInstance()?.model?.schedule_list?.value!![i]
                val weather_model = weather_iter?.next()

                val sky = (weather_model?.sky ?: "")
                var sky_string = ""
                if (sky == "0") {
                    sky_string = "맑음"
                } else if (sky == "1") {
                    sky_string = "비"
                } else if (sky == "2") {
                    sky_string = "진눈깨비"
                } else if (sky == "3") {
                    sky_string = "눈"
                } else if (sky == "4") {
                    sky_string = "소나기"
                }

                val GROUP_KEY_B = "com.example.dayday.B"
                
                if (i == 0) {
                    var builder_schedule_1 = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    var builder_schedule_2 = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
                        .setContentIntent(pendingIntent)
                        .build()

                    notification_list.add(builder_schedule_1)
                    notification_list.add(builder_schedule_2)
                }
                else {
                    var builder_schedule = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
                        .setContentIntent(pendingIntent)
                        .build()

                    notification_list.add(builder_schedule)
                }
            }

            val weather_model = weather_iter?.next()

            val sky = (weather_model?.sky ?: "")
            var sky_string = ""
            if (sky == "0") {
                sky_string = "맑음"
            } else if (sky == "1") {
                sky_string = "비"
            } else if (sky == "2") {
                sky_string = "진눈깨비"
            } else if (sky == "3") {
                sky_string = "눈"
            } else if (sky == "4") {
                sky_string = "소나기"
            }

            i = 1000
            for (notifi in notification_list) {
                notificationManager?.notify(i, notifi)
                i++
            }
            
        }
        else if (workName == WORK_B_NAME) {
            // 2022-05-13 변경 사항 : S+ 버전 이상에서는 FLAG_MUTABLE 또는 FLAG_IMMUTABLE을 붙여줘야 함.
            val pendingIntent = PendingIntent.getActivity(
                mContext,
                WORK_B_NOTIFICATION_CODE,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )

            // 2022-06-09 예제용 코드 제거
//            notificationBuilder.setContentTitle("WorkerB Notification")
//                .setContentText("set a Notification contents")
//                .setContentIntent(pendingIntent)
//            notificationManager?.notify(WORK_B_NOTIFICATION_CODE, notificationBuilder.build())

            // 2022-06-09 MainActivity에서 알림을 생성하는 코드를 가져옴
            val weather_iter = MainActivity.getInstance()?.model?.weather_data?.value?.iterator()
            val notification_list = mutableListOf<Notification>()
            var i = 0
            for (i in 0 until (MainActivity.getInstance()?.model?.schedule_list?.value!!.size)) {

                var schedule = MainActivity.getInstance()?.model?.schedule_list?.value!![i]
                val weather_model = weather_iter?.next()

                val sky = (weather_model?.sky ?: "")
                var sky_string = ""
                if (sky == "0") {
                    sky_string = "맑음"
                } else if (sky == "1") {
                    sky_string = "비"
                } else if (sky == "2") {
                    sky_string = "진눈깨비"
                } else if (sky == "3") {
                    sky_string = "눈"
                } else if (sky == "4") {
                    sky_string = "소나기"
                }

                val GROUP_KEY_B = "com.example.dayday.B"

                if (i == 0) {
                    var builder_schedule_1 = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(true)
                        .setContentIntent(pendingIntent)
                        .build()

                    var builder_schedule_2 = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
                        .setContentIntent(pendingIntent)
                        .build()

                    notification_list.add(builder_schedule_1)
                    notification_list.add(builder_schedule_2)
                }
                else {
                    var builder_schedule = notificationBuilder
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
                        .setContentIntent(pendingIntent)
                        .build()

                    notification_list.add(builder_schedule)
                }
            }

            val weather_model = weather_iter?.next()

            val sky = (weather_model?.sky ?: "")
            var sky_string = ""
            if (sky == "0") {
                sky_string = "맑음"
            } else if (sky == "1") {
                sky_string = "비"
            } else if (sky == "2") {
                sky_string = "진눈깨비"
            } else if (sky == "3") {
                sky_string = "눈"
            } else if (sky == "4") {
                sky_string = "소나기"
            }

            i = 1000
            for (notifi in notification_list) {
                notificationManager?.notify(i, notifi)
                i++
            }
        }
    }

    companion object {
        private const val WORK_A_NOTIFICATION_CODE = 0
        private const val WORK_B_NOTIFICATION_CODE = 1
        fun setScheduledNotification(workManager: WorkManager) {
            setANotifySchedule(workManager)
            setBNotifySchedule(workManager)
        }

        private fun setANotifySchedule(workManager: WorkManager) {
            // Event 발생시 WorkerA.class 호출
            // 알림 활성화 시점에서 반복 주기 이전에 있는 가장 빠른 알림 생성
            val aWorkerOneTimePushRequest = OneTimeWorkRequest.Builder(WorkerA::class.java).build()
            // 가장 가까운 알림시각까지 대기 후 실행, 12시간 간격 반복 5분 이내 완료
            val aWorkerPeriodicPushRequest = PeriodicWorkRequest.Builder(
                WorkerA::class.java, 12, TimeUnit.HOURS, 5, TimeUnit.MINUTES
            )
                .build()
            try {
                // workerA 정보 조회
                val aWorkerNotifyWorkInfoList =
                    workManager.getWorkInfosForUniqueWorkLiveData(WORK_A_NAME).value!!
                for (workInfo in aWorkerNotifyWorkInfoList) {
                    // worker의 동작이 종료된 상태라면 worker 재등록
                    if (workInfo.state.isFinished) {
                        workManager.enqueue(aWorkerOneTimePushRequest)
                        workManager.enqueueUniquePeriodicWork(
                            WORK_A_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            aWorkerPeriodicPushRequest
                        )
                    }
                }
            }
            catch (nullPointerException: NullPointerException) {
                // 알림 worker가 생성된 적이 없으면 worker 생성
                workManager.enqueue(aWorkerOneTimePushRequest)
                workManager.enqueueUniquePeriodicWork(
                    WORK_A_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    aWorkerPeriodicPushRequest
                )
            }
        }

        private fun setBNotifySchedule(workManager: WorkManager) {
            // Event 발생 시 WorkerB.class 호출
            val bWorkerOneTimePushRequest = OneTimeWorkRequest.Builder(WorkerB::class.java).build()
            val bWorkerPeriodicPushRequest = PeriodicWorkRequest.Builder(
                WorkerB::class.java, 12, TimeUnit.HOURS, 5, TimeUnit.MINUTES
            )
                .build()
            try {
                val bWorkerNotifyWorkInfoList =
                    workManager.getWorkInfosForUniqueWorkLiveData(WORK_B_NAME).value!!
                for (workInfo in bWorkerNotifyWorkInfoList) {
                    if (workInfo.state.isFinished) {
                        workManager.enqueue(bWorkerOneTimePushRequest)
                        workManager.enqueueUniquePeriodicWork(
                            WORK_B_NAME,
                            ExistingPeriodicWorkPolicy.KEEP,
                            bWorkerPeriodicPushRequest
                        )
                    }
                }
            }
            catch (nullPointerException: NullPointerException) {
                workManager.enqueue(bWorkerOneTimePushRequest)
                workManager.enqueueUniquePeriodicWork(
                    WORK_B_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    bWorkerPeriodicPushRequest
                )
            }
        }

        // 현재시각이 알림 범위에 해당하지 않으면 딜레이 리턴
        fun getNotificationDelay(workName: String): Long {
            var pushDelayMillis: Long = 0
            val cal = Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA)
            val currentMillis = cal.timeInMillis
            if (workName == WORK_A_NAME) {
                // 2022-06-09 입력받은 시각으로 기존 고정된 시각 대체
//                var formatter = SimpleDateFormat("hh:mm:ss")
                // 2022-06-21 알림시간 설정 위젯에서 설정을 가져올 수 있도록 설정
                var str = MainActivity.getInstance()?.alarmTime?.text.toString()

                // #!
                Log.i("DEBUG ALARM A", str)

                if (str.isNullOrEmpty()) {
                    str = "00:00:00"
                }
                var date = str


                // 현재 시각이 20:00보다 크면 다음 날 오전 알림, 현재 시각이 20:00 전인지 08:00 전인지에 따라 알림 딜레이 설정
                if (cal[Calendar.HOUR_OF_DAY] >= Constants.A_NIGHT_EVENT_TIME) {
                    val nextDayCal = getScheduledCalender(date)
                    nextDayCal.add(Calendar.DAY_OF_YEAR, 1)
                    pushDelayMillis = nextDayCal.timeInMillis - currentMillis
                }
                else if (cal[Calendar.HOUR_OF_DAY] >= A_MORNING_EVENT_TIME && cal[Calendar.HOUR_OF_DAY] < A_NIGHT_EVENT_TIME) {
                    pushDelayMillis =
                        getScheduledCalender(date).timeInMillis - currentMillis
                }
                else if (cal[cal[Calendar.HOUR_OF_DAY]] < A_MORNING_EVENT_TIME) {
                    pushDelayMillis =
                        getScheduledCalender(date).timeInMillis - currentMillis
                }
                else {
                    // 2022-06-21 그 외의 경우에 알림이 뜨도록 추가
                    pushDelayMillis = getScheduledCalender(date).timeInMillis - currentMillis
                }
                
            }
//            else if (workName == WORK_B_NAME) {
//                // 2022-06-09 입력받은 시각으로 기존 고정된 시각 대체
////                var formatter = SimpleDateFormat("hh:mm:ss")
//                var str = MainActivity.getInstance()?.findViewById<EditText>(R.id.alarmTime2)?.text.toString()
//                if (str.isNullOrEmpty()) {
//                    str = "00:00:00"
//                }
//                // #!
//                Log.i("DEBUG ALARM B", str)
//
//                var date = str
//
//                // 현재 시각이 21:00보다 크면 다음 날 오전 알림, 현재 시각이 21:00 전인지 09:00 전인지에 따라 알림 딜레이 설정
//                if (cal[Calendar.HOUR_OF_DAY] >= B_NIGHT_EVENT_TIME) {
//                    val nextDayCal = getScheduledCalender(date)
//                    nextDayCal.add(Calendar.DAY_OF_YEAR, 1)
//                    pushDelayMillis = nextDayCal.timeInMillis - currentMillis
//                }
//                else if (cal[Calendar.HOUR_OF_DAY] >= B_MORNING_EVENT_TIME && cal[Calendar.HOUR_OF_DAY] < B_NIGHT_EVENT_TIME) {
//                    pushDelayMillis =
//                        getScheduledCalender(date).timeInMillis - currentMillis
//                }
//                else if (cal[cal[Calendar.HOUR_OF_DAY]] < B_MORNING_EVENT_TIME) {
//                    pushDelayMillis =
//                        getScheduledCalender(date).timeInMillis - currentMillis
//                }
//            }
            return pushDelayMillis
        }

        fun isNotificationChannelCreated(context: Context): Boolean {
            return try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    return notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) != null
                }
                true
            }
            catch (nullException: NullPointerException) {
                Toast.makeText(context, "푸시 알림 기능에 문제가 발생했습니다. 앱을 재실행해주세요.", Toast.LENGTH_SHORT)
                    .show()
                false
            }
        }

        // 푸시 알림 허용 및 사용자에 의해 알림이 꺼진 상태가 아니라면 푸시 알림 백그라운드 갱신
        fun refreshScheduledNotification(context: Context) {
            try {
                val isNotificationActivated =
                    getBoolean(context, Constants.SHARED_PREF_NOTIFICATION_KEY)
                if (isNotificationActivated) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    val isNotifyAllowed: Boolean
                    isNotifyAllowed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channelImportance =
                            notificationManager.getNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID).importance
                        channelImportance != NotificationManager.IMPORTANCE_NONE
                    }
                    else {
                        NotificationManagerCompat.from(context).areNotificationsEnabled()
                    }
                    if (isNotifyAllowed) {
                        setScheduledNotification(WorkManager.getInstance(context))
                    }
                }
            }
            catch (nullException: NullPointerException) {
                Toast.makeText(context, "푸시 알림 기능에 문제가 발생했습니다. 앱을 재실행해주세요.", Toast.LENGTH_SHORT)
                    .show()
                nullException.printStackTrace()
            }
        }

        // 한번 실행 시 이후 재호출해도 동작 안함
        fun createNotificationChannel(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val notificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    // NotificationChannel 초기화
                    val notificationChannel = NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        context.getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )

                    // Configure the notification channel
                    notificationChannel.description = "푸시알림"
                    notificationChannel.enableLights(true) // 화면활성화 설정
                    notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500) // 진동패턴 설정
                    notificationChannel.enableVibration(true) // 진동 설정
                    notificationManager.createNotificationChannel(notificationChannel) // channel 생성
                }
            }
            catch (nullException: NullPointerException) {
                // notificationManager null 오류 raise
                Toast.makeText(
                    context,
                    "푸시 알림 채널 생성에 실패했습니다. 앱을 재실행하거나 재설치해주세요.",
                    Toast.LENGTH_SHORT
                ).show()
                nullException.printStackTrace()
            }
        }

        // 2022-06-09 원활한 시각 설정을 위해서 String 타입으로 변경. 원래는 Date! 타입이었음.
        fun getScheduledCalender(scheduledTime: String): Calendar {
            val cal = Calendar.getInstance(TimeZone.getTimeZone(KOREA_TIMEZONE), Locale.KOREA)
            val splitted_time = scheduledTime!!.split(":")

            cal[Calendar.HOUR_OF_DAY] = splitted_time[0].toInt()
            cal[Calendar.MINUTE] = splitted_time[1].toInt()
            cal[Calendar.SECOND] = 0

            // #!
            Log.i("DEBUG ALRAM", cal.time.toString())

            return cal
        }
    }
}
