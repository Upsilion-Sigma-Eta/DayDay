package com.example.dayday

import androidx.appcompat.app.AppCompatActivity
import android.widget.CompoundButton
import androidx.work.WorkManager
import android.os.Bundle
import android.content.Intent
import android.view.View
import android.widget.Button

class SecondActivity : AppCompatActivity() {
    //lateinit var btnSecondToMain: Button
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
                } else {
                    NotificationHelper.createNotificationChannel(applicationContext)
                }
            } else {
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
        setContentView(R.layout.activity_main)
        //btnSecondToMain = findViewById(R.id.btn_second_go_main)
       // btnSecondToMain.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        //})
        initSwitchLayout(WorkManager.getInstance(applicationContext))
    }
}
