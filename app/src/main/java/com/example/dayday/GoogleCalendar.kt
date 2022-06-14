package com.example.dayday

import android.app.ProgressDialog
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import java.util.*

class GCalendar {
    //Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    var google_service: Calendar? = null

    //Google GCalendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    var mID = 0
    var mCredential: GoogleAccountCredential? = null
    //    var mStatusText: TextView? = null
//    var mResultText: TextView? = null
//    var mGetEventButton: Button? = null
//    var mAddEventButton: Button? = null
//    var mAddCalendarButton: Button? = null
//    var mPanelSlide: Button? = null
    var mProgress: ProgressDialog? = null

    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                      GCalendar                                      //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** GoogleCalendar의 main *******************************************************************/
    public fun mainGoogleCalendar() {

        // Google GCalendar API의 호출 결과를 표시하는 TextView를 준비
//        mResultText!!.isVerticalScrollBarEnabled = true
//        mResultText!!.movementMethod = ScrollingMovementMethod()
//        mStatusText!!.isVerticalScrollBarEnabled = true
//        mStatusText!!.movementMethod = ScrollingMovementMethod()
//        mStatusText!!.text = "버튼을 눌러 테스트를 진행하세요."


        // Google GCalendar API 호출중에 표시되는 ProgressDialog
        mProgress = ProgressDialog(MainActivity.getInstance())
        mProgress!!.setMessage("Google GCalendar API 호출 중입니다.")


        // Google GCalendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            MainActivity.getInstance()?.applicationContext,
            Arrays.asList(*MainActivity.SCOPES)
        ).setBackOff(ExponentialBackOff()) // I/O 예외 상황을 대비해서 백오프 정책 사용
    }

}