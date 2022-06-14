package com.example.dayday

object Constants {
    // 알림 설정 Preference Key 값
    const val SHARED_PREF_NOTIFICATION_KEY = "Notification Value"
    // 2022-06-09 백그라운드 노티피케이션을 위한 값 추가
    const val SHARED_PREF_NOTIFICATION_KEY_A = "Notification Value A"
    const val SHARED_PREF_NOTIFICATION_KEY_B = "Notification Value B"

    // 알림 채널 ID 값
    const val NOTIFICATION_CHANNEL_ID = "10001"

    // 한국 TimeZone
    const val KOREA_TIMEZONE = "Asia/Seoul"

    // 챌린지 랭킹 시작 시각
    const val A_MORNING_EVENT_TIME = 1
    const val A_NIGHT_EVENT_TIME = 13
    const val B_MORNING_EVENT_TIME = 2
    const val B_NIGHT_EVENT_TIME = 14

    // 푸시알림 허용 Interval 시간
    const val NOTIFICATION_INTERVAL_HOUR = 1

    // 백그라운드 work Unique 이름
    const val WORK_A_NAME = "Challenge Notification"
    const val WORK_B_NAME = "Ranking Notification"
}
