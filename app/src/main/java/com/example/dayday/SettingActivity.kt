/*************************************************************************************************
 *
 *  0. Project: DayDay
 *     달력에 일주일 날씨를 함께 보여주는 앱이다.
 *     알림 설정 시 일정과 날씨를 상단알림바 기능으로 확인할 수 있다.
 *
 *  1. 작성자: 윤상현, 이선아, 정지훈
 *  2. 작성일: 2022.05.10
 *  3. 버전: v.1.0.0-alpha
 *  4. 변경 이력
 *    이름     :     일자      :   변경내용
 * -----------------------------------------------------------------------------------------------
 *   정지훈		2022.05.10		정해진 시각에 알림 띄우기
 *   이선아		2022.05.12		정해진 시각에 알림 띄우기 DayDay로 단순 옮김
 *   정지훈		2022.05.22		SettingActivity.kt, activity_setting.xml 활성화
 *								정해진 시각에 알림이 뜨도록 하였음.
 *								파일 이름에 변동이 생겼으니 README.md 파일을 잘 숙지해주시기 바람.
 *
 *************************************************************************************************/

package com.example.dayday

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CompoundButton
import android.widget.TextView
import androidx.work.WorkManager

class SettingActivity : AppCompatActivity() {
    lateinit var btnSecondToMain: Button
    var switchActivateNotify: CompoundButton? = null

    // 푸시알림 설정
    private fun initSwitchLayout(workManager: WorkManager) {
        switchActivateNotify = findViewById<View>(R.id.switch_second_notify) as CompoundButton
        switchActivateNotify!!.isChecked =
            PreferenceHelper.getBoolean(applicationContext, Constants.SHARED_PREF_NOTIFICATION_KEY)
        switchActivateNotify!!.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val isChannelCreated: Boolean = NotificationHelper.isNotificationChannelCreated(
                    applicationContext
                )
                if (isChannelCreated) {
                    PreferenceHelper.setBoolean(
                        applicationContext,
                        Constants.SHARED_PREF_NOTIFICATION_KEY,
                        true
                    )
                    NotificationHelper.setScheduledNotification(workManager)
                }
                else {
                    NotificationHelper.createNotificationChannel(applicationContext)
                }
            }
            else {
                PreferenceHelper.setBoolean(
                    applicationContext,
                    Constants.SHARED_PREF_NOTIFICATION_KEY,
                    false
                )
                workManager.cancelAllWork()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        btnSecondToMain = findViewById(R.id.btn_second_go_main)
        btnSecondToMain.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        })
        initSwitchLayout(WorkManager.getInstance(applicationContext))
    }
}
