/*************************************************************************************************
 *
 *  0. Project: DayDay
 *     달력에 일주일 날씨를 함께 보여주는 앱이다.
 *     알림 설정 시 일정과 날씨를 상단알림바 기능으로 확인할 수 있다.
 *
 *  1. 작성자: 윤상현, 이선아, 정지훈
 *  2. 작성일: 2022.06.05
 *  3. 버전: v.1.0.0-alpha
 *  4. 변경 이력
 *    이름     :     일자      :   변경내용
 * -----------------------------------------------------------------------------------------------
 *   정지훈		2022.04.25		Notification 여러개 출력
 *   윤상현		2022.04.29		더미데이터(testDummy@gmail.com.ics)읽고 Notification에 출력
 *   이선아		2022.05.01		소스 정리, Notification 아이콘 생성, 구글 API 생성
 *   이선아		2022.05.02		2022.04.25 Notification 복구
 *	 윤상현		2022.05.05		리사이클러 뷰를 이용해서 일정 리스트 1차 구현
 *   정지훈		2022.05.10		정해진 시각에 알림 띄우기, 일정 데이터와 날씨 더미 데이터 알림에 띄우기
 *                              ( DayDay의 MainActivity에서 한 일이 아님. )
 *   이선아		2022.05.12		정해진 시각에 알림 띄우기 DayDay로 단순 옮김, 사용자 현재 위치의 Weather
 *                              ( 백엔드 fin, 프론트엔드 현재 갱신된 날씨 정보가 리사이클러 뷰에 전달 안 됨. )
 *   정지훈		2022.05.21		Google Calendar사용은 GCalendar 사용 금지하고, MainActivity.kt파일에서 처리할 것.
 *								Google Calendar에서 정보 가져오는데 성공. 오늘 날짜부터 미래의 일정을 가져오는데는 .setTimeMin(now) 주석 해제하면 됨. 995번째 줄 참고.
 *								정해진 시간에 알림 생성 가능하도록 하였음. 자세한 사항은 README.md 파일 참고.
 *	 윤상현		2022.05.27      구글 캘린더에서 가져온 데이터와 레이아웃 연동
 *	                            날씨 API로 가져온 날씨 정보에 따라 적절한 아이콘이 표시되도록 수정 => 날씨 타입이 어떻게 되는지에 대한 추가 정보 필요
 *	 정지훈		2022.05.30		부모 알림 - 자식 알림 띄우기
 * 								ReadDummyFile(), showNotification() 주석처리
 *	 이선아		2022.05.30LSA	Google Geocoding API 연결
 *								화면 레이아웃 1차 변경(카드 데이터 단순화)
 *								화면에 현재 위치 주소, 오늘 날짜, 현재 온도 표시
 *	 이선아		2022.06.05		디자인 레이아웃 수정
 *								날씨 API 추가 및 수정(초단기예보->단기예보)
 *								위치 API 추가, 주소 검색 기능 추가, 리사이클러 뷰 추가 및 데이터 연결
 *	 윤상현		2022.06.09		노티피케이션이 백그라운드에서 뜨도록 수정
 *								알람 시각 설정 기능 및 여부 추가
 *	 이선아      2022.06.20      Notification 키보드 입력->TimerUI 사용, 이전 설정 내용 저장
 *	                            schedule list 어플리케이션 실행하면 자동 갱신
 *************************************************************************************************/


package com.example.dayday

//Calendar


//Google Calendar
//import com.google.api.services.calendar.Calendar


//기상청 동네예보 API


//Location


import android.Manifest
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.app.*
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Point
import android.location.Address
import android.location.Geocoder
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkManager
import com.example.dayday.databinding.ActivityMainBinding
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.*
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.CalendarList
import com.google.api.services.calendar.model.Event
import com.google.api.services.calendar.model.EventDateTime
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Call
import retrofit2.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity() {
    //////////////////////////////////////////////////////////////////////////////////////////////
    //                        Schedule RecyclerView Global Variables                            //
    //////////////////////////////////////////////////////////////////////////////////////////////
    // 2922-05-28 private 선언 제거. 혹시라도 차후에 사용할 일이 있을까 싶어서. 제거하였음
    lateinit var model: ScheduleViewModel
    lateinit var member_schedule_adapter: ScheduleAdapter
    // 2022-06-09 ViewBinding 활용을 위해서 binding 선언
    lateinit var binding: ActivityMainBinding

    // 2022-06-21 알람 시각 설정을 위한 변수 선언
    var alarmTime: TextView? = null

    // 메인 액티비티 인스턴스 생성
    init {
        instance = this
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //                        Alarm Related Variables                                           //
    //////////////////////////////////////////////////////////////////////////////////////////////
    // 2022-06-09 알람 세팅 보존을 위한 변수 선언
    lateinit var m_switch_alarm_1 : CompoundButton
    lateinit var m_switch_alarm_2 : CompoundButton

    //////////////////////////////////////////////////////////////////////////////////////////////
    //                        Weather RecyclerView Global Variables                             //
    //////////////////////////////////////////////////////////////////////////////////////////////
    lateinit var member_weather_adapter: WeatherScheduleAdapter



    companion object {
        const val TAG = "DayDay"            //app notification name
        const val NOTIFICATION_ID = 1001    //app notification id

        const val REQUEST_ACCOUNT_PICKER = 1000
        const val REQUEST_AUTHORIZATION = 1001
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1002
        const val REQUEST_PERMISSION_GET_ACCOUNTS = 1003
//        private const val PREF_ACCOUNT_NAME = "accountName"
//        private val SCOPES = arrayOf(CalendarScopes.CALENDAR)

        const val PREF_ACCOUNT_NAME = "accountName"
        val SCOPES = arrayOf(CalendarScopes.CALENDAR)


        // 메인 액티비티의 콘텍스트를 가져오기 위해서 인스턴스 생성
        @SuppressLint("StaticFieldLeak")
        private var instance: MainActivity? = null

        fun getInstance(): MainActivity? {
            return instance
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    //                              Weather API Global Variables                                //
    //////////////////////////////////////////////////////////////////////////////////////////////
    lateinit var tvDate : TextView   // 오늘 날짜
    lateinit var base_date : String  // 발표 일자
    lateinit var base_time : String  // 발표 시각

    private var curPoint : Point? = null    // 현재 위치의 격자 좌표를 저장할 포인트
    public var cur_x : Double = 0.0
    public var cur_y : Double = 0.0

    // 현재 시간부터 1시간 뒤의 날씨를 일정의 개수만큼 담을 배열
    // 2022.06.02 상단 가로 리사이클러뷰에 사용할 weatherArr(9개)
    //            최대 3일(72개)까지 불러올 수 있음.
    val weatherArr = arrayOf(
        ModelWeather(), ModelWeather(), ModelWeather(),
        ModelWeather(), ModelWeather(), ModelWeather(),
        ModelWeather(), ModelWeather(), ModelWeather())

    var weatheritem : List<ITEM>? = emptyList()



    //////////////////////////////////////////////////////////////////////////////////////////////
    //                          Google Calendar API Global Variables                            //
    //////////////////////////////////////////////////////////////////////////////////////////////
    // 구글 캘린더 클래스
    //private var google_calendar = GCalendar()

    //Google Calendar API에 접근하기 위해 사용되는 구글 캘린더 API 서비스 객체
    var google_service: com.google.api.services.calendar.Calendar? = null

    //Google Calendar API 호출 관련 메커니즘 및 AsyncTask을 재사용하기 위해 사용
    private var mID = 3
    var mCredential: GoogleAccountCredential? = null
//    private var mStatusText: TextView? = null
//    private var mResultText: TextView? = null
//    private var mGetEventButton: Button? = null
//    private var mAddEventButton: Button? = null
//    private var mAddCalendarButton: Button? = null
//    private var mPanelSlide: Button? = null
    var mProgress: ProgressDialog? = null



    //////////////////////////////////////////////////////////////////////////////////////////////
    //                            Notification Time Global Variables                            //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //lateinit var btnMainToSetting: Button
    var isFinished = true




    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                       Google Location                                    //
    //////////////////////////////////////////////////////////////////////////////////////////////
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    private val multiplePermissionsCode = 100

    //필요한 퍼미션 리스트
    private val requiredPermissions = arrayOf(
        Manifest.permission.GET_ACCOUNTS,
        Manifest.permission.ACCESS_COARSE_LOCATION)




    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////   /////////////////////////   //////////////////////////////
    /////////////////////////////////   /////////////////////////   //////////////////////////////
    /////////////////////////////////   /////////////////////////   //////////////////////////////
    /////////////////////////                   /////////                   //////////////////////
    ////////////////////////////             ///////////////             /////////////////////////
    ////////////////////////////////     ///////////////////////     /////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2022-06-09 ViewBinding 활용.
        binding = ActivityMainBinding.inflate(layoutInflater)
        alarmTime = findViewById(R.id.alarmTime)

        SetPermission()

        mainSetLocation()
        mainScheduleView()
        mainGoogleCalendar()
        mainNotification()
        mainNotificationTime()

        //    var date : TextView = findViewById<View>(R.id.date) as TextView
        // date.text = "5월 21일"

        // 구글 캘린더 설정 부분. 리사이클러뷰가 되면서 버튼 기능들은 존재하지 않게 됨.
        // 아래 부분은 메인 액티비티에서 기존에 쓰이던 값들을 초기화한 구글 캘린더 값들로 채워넣는 역할을 함.
//        google_calendar.mainGoogleCalendar()
//
//        google_service = google_calendar.google_service
//        mID = google_calendar.mID
//        mCredential = google_calendar.mCredential
//        mStatusText = google_calendar.mStatusText
//        mResultText = google_calendar.mResultText
//        mGetEventButton = google_calendar.mGetEventButton
//        mAddEventButton = google_calendar.mAddEventButton
//        mAddCalendarButton = google_calendar.mAddCalendarButton
////        mPanelSlide = google_calendar.mPanelSlide
//        mProgress = google_calendar.mProgress

    }
    ///////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////













    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                       Google Location                                    //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                       permission                                         //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** set permission **************************************************************************/
    fun mainSetLocation(){
        //2022.06.02 location_icon: 현재 위치
        //           location_text: 선택


        location_icon.setOnClickListener(userLocationListener())
        location_text.setOnClickListener(addrLocationListener())
        if(!location_icon.isSelected && !location_text.isSelected&&
            ContextCompat.checkSelfPermission(this, requiredPermissions[1]) == PackageManager.PERMISSION_GRANTED)
            requestLocation()
        loadData()
    }

    /** 첫 실행시 위치 권한 요청 ********************************************************************/
    fun SetPermission(){

        var rejectedPermissionList = ArrayList<String>()

        //필요한 퍼미션들을 하나씩 끄집어내서 현재 권한을 받았는지 체크
        for(permission in requiredPermissions){
            if(ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //만약 권한이 없다면 rejectedPermissionList에 추가
                rejectedPermissionList.add(permission)
            }
        }
        //거절된 퍼미션이 있다면...
        if(rejectedPermissionList.isNotEmpty()){
            //권한 요청!
            val array = arrayOfNulls<String>(rejectedPermissionList.size)
            ActivityCompat.requestPermissions(this, rejectedPermissionList.toArray(array), multiplePermissionsCode)
        }

//        //어플 시작 전 위치권한 설정
//        var fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
//
//        if(ActivityCompat.checkSelfPermission(
//                this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//            || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//            != PackageManager.PERMISSION_GRANTED){
//            var permissions = arrayOf(
//                android.Manifest.permission.ACCESS_FINE_LOCATION,
//                android.Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//            ActivityCompat.requestPermissions(this, permissions, 10)
//        }

    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Save & Load Data                                        //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 위치 정보 저장 ****************************************************************************/
    private fun saveData(){
        val pref = this.getPreferences(0)
        var settime = findViewById<TextView>(R.id.alarmTime)
        val editor=pref.edit()

        editor.putBoolean("KEY_ICON",location_icon.isSelected)  //현재 위치 Selected(T/F)
            .putBoolean("KEY_TEXT",location_text.isSelected)    //선택 위치 Selected(T/F)
            .putFloat("KEY_X", cur_x.toFloat())                    //x좌표
            .putFloat("KEY_Y", cur_y.toFloat())                    //y좌표
            .putString("KEY_NOTI", settime.text.toString())     //알림 시간
            .apply()
    }

    /** 위치 정보 불러오기 *************************************************************************/
    private fun loadData(){

        var settime = findViewById<TextView>(R.id.alarmTime)

        val pref=this.getPreferences(0)
        // 키에 해당되는 밸류를 가져오는데 저장된 값이 없으면 0을 가져온다
        val kicon=pref.getBoolean("KEY_ICON",false)
        val ktext=pref.getBoolean("KEY_TEXT",false)
        var kx=pref.getFloat("KEY_X", 36.119485F)
        var ky=pref.getFloat("KEY_Y", 37.5666805F)
        var ktime=pref.getString("KEY_NOTI", "12:00")

        location_icon.isSelected = kicon
        location_text.isSelected = ktext
        curPoint = dfs_xy_conv(kx.toDouble(), ky.toDouble())

        //이전에 현재 위치를 고른 경우
        if( kicon ) {
            requestLocation()
        }
        //이전에 선택 위치를 고른 경우
        if( ktext ) {
            if(kx!=0F && ky!=0F) {
                setWeather(kx.toInt(), ky.toInt())
                var currentAddr = setXYtoAddr(kx.toDouble(), ky.toDouble()).split(' ')
                var addr = ""
                for(i in 3..currentAddr.size)
                    addr += currentAddr[i-1] + " "
                location_text.text = addr
            }
            else
                location_text.text = "주소를 입력해주세요."
        }


        settime.text = ktime
    }




    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                       Addr <-> XY                                        //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 위도,경도를 주소로 **************************************************************************/
    fun setXYtoAddr(nx : Double, ny : Double): String {
        var addressList: List<Address>? = null
        val geocoder = Geocoder(this, Locale.getDefault())

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        addressList = try {
            geocoder.getFromLocation(nx.toDouble(), ny.toDouble(), 1)
        } catch (e: IOException) {
            Toast.makeText(
                this,
                "위치로부터 주소를 인식할 수 없습니다. 네트워크가 연결되어 있는지 확인해주세요.",
                Toast.LENGTH_SHORT
            ).show()
            e.printStackTrace()
            return "주소 인식 불가"
        }
        if(addressList?.size!! <1){
            return "해당 위치에 주소 없음"
        }

        //주소를 담는 문자열을 생성하고 리턴
        val address = addressList[0]
        val addressStringBuilder = StringBuilder()
        for ( i in 0..address.maxAddressLineIndex){
            addressStringBuilder.append(address.getAddressLine(i))
            if(i<address.maxAddressLineIndex) addressStringBuilder.append("\n")
        }
        return addressStringBuilder.toString()

    }

    /** 주소를 위도,경도로 **************************************************************************/
    fun setAddrtoXY(addr : String): Boolean {
        val geocoder = Geocoder(this, Locale.getDefault())
        var results : List<Address>? = null

        try{
            results = geocoder.getFromLocationName(addr, 10)
        }catch (e : ActivityNotFoundException){
            e.printStackTrace()
            Log.e("setAddrtoXY error","")
        }
        if (results != null) {
            if (results.isEmpty()){
                Toast.makeText(this@MainActivity,
                    "주소가 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
            }else{
                //위도 경도 호출 성공
                curPoint = dfs_xy_conv(results[0].latitude, results[0].longitude)

                val location_text : TextView = findViewById<View>(R.id.location_text) as TextView
                val currentAddr = setXYtoAddr(results[0].latitude, results[0].longitude).split(' ')
                var addr = ""
                for(i in 3..currentAddr.size)
                    addr += currentAddr[i-1] + " "
                location_text.text = addr
                cur_x=results[0].latitude
                cur_y=results[0].longitude
                saveData()

                return true
            }
        }
        return false
    }




    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                   Set Location Button                                    //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 현재 위치 요청 버튼 *************************************************************************/
    inner class userLocationListener : View.OnClickListener{
        override fun onClick(v: View?) {

            location_icon.isSelected = true
            location_text.isSelected = false

            requestLocation()
        }

    }

    /** 선택 위치 요청 버튼 *************************************************************************/
    inner class addrLocationListener : View.OnClickListener{
        override fun onClick(v: View?){
            location_icon.isSelected = false
            location_text.isSelected = true

            val location_text : TextView = findViewById<View>(R.id.location_text) as TextView
            val edit_view = EditText(this@MainActivity)
            val button = Button(this@MainActivity)


            edit_view.let{
                edit_view.width = 350
                edit_view.inputType = TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
            button.let{
                button.setBackgroundColor(Color.WHITE)
                button.text = "검색"
            }

            if( location_text.isSelected==true){
                location_text.visibility = View.GONE
                location_text.isSelected = true
                button.setOnClickListener(InputButtonListener(edit_view, button))
                location_bar.addView(edit_view)
                location_bar.addView(button)
            }

        }
    }

    /** 주소 검색 요청 버튼 *************************************************************************/
    inner class InputButtonListener(var edit_view:EditText, var button:Button) : View.OnClickListener{
        @SuppressLint("SetTextI18n")
        override fun onClick(v: View?){
            val location_text : TextView = findViewById<View>(R.id.location_text) as TextView
            var addr_text = edit_view.text.toString()
            //var buff = "2, Busandaehak-ro 63beon-gil, Geumjeong-gu, Busan, Republic of Korea"
            if(addr_text.isNotEmpty()){
                if(setAddrtoXY(addr_text)) {
                    setWeather(curPoint!!.x, curPoint!!.y)
                }
                else{
                    //아무 일도 하지 않음
                }
            }

            CloseKeyboard()

            location_text.visibility = View.VISIBLE
            location_bar.removeView(edit_view)
            location_bar.removeView(button)
        }
    }

    /** EditText의 Keyboard Error 해결 함수 ********************************************************/
    fun CloseKeyboard()
    {
        var view = this.currentFocus

        if(view != null)
        {
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

//    // Lccation permission 설정
//    private fun LocationSetPermission() {
//        // 권한 묻는 팝업 만들기
//        val permissionListener = object : PermissionListener {
//            // 설정해놓은 권한을 허용됐을 때
//            override fun onPermissionGranted() {
//                // 1초간 스플래시 보여주기
//                 //val backgroundExecutable : ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
//                 //val mainExecutor : Executor = ContextCompat.getMainExecutor(this@MainActivity)
//                 //backgroundExecutable.schedule({
//
//                        // MainActivity 넘어가기
//                        //finish()
//                        //val intent = Intent(this@MainActivity, MainActivity::class.java)
//                        //startActivity(intent)
//                        //finish()
//
//
//
////}, 1, TimeUnit.SECONDS)
//
//                Toast.makeText(this@MainActivity, "권한 허용", Toast.LENGTH_SHORT).show()
//                //아무 일도 하지 않음
//            }
//
//            // 설정해놓은 권한을 거부됐을 때
//            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
//                // 권한 없어서 요청
//                AlertDialog.Builder(this@MainActivity)
//                    .setMessage("권한 거절로 인해 기능이 제한됩니다.")
//                    .setPositiveButton("권한 설정하러 가기") { dialog, which ->
//                        try {
//                            startActivity(intent)
//                        } catch (e : ActivityNotFoundException) {
//                            e.printStackTrace()
//                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
//                            startActivity(intent)
//                        }
//                    }
//                    .show()
//
//                Toast.makeText(this@MainActivity, "권한 거부", Toast.LENGTH_SHORT).show()
//            }
//
//        }
//
//        // 권한 설정
//        TedPermission.with(this@MainActivity)
//            .setPermissionListener(permissionListener)
//            .setRationaleMessage("정확한 날씨 정보를 위해 권한을 허용해주세요.")
//            .setDeniedMessage("권한을 거부하셨습니다. [앱 설정]->[권한] 항목에서 허용해주세요.")
//                //서버에 요청 후 항상 허용(ACCESS_BACKGROUND_LOCATION)으로 사용할 예정
//                //https://developer.android.com/training/location/permissions?hl=ko
//            .setPermissions(android.Manifest.permission.ACCESS_COARSE_LOCATION) // 필수 권한만 문기
//            .check()
//    }














    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                  Schedule RecyclerView                                   //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** ScheduleView의 main **********************************************************************/
    fun mainScheduleView(){

        //<----------------------------------weather 적용-------------------------------------->//
        //var location_text : TextView = findViewById<View>(R.id.location) as TextView

        //tvDate = findViewById(R.id.date)   // 오늘 날짜 텍스트뷰

        //에뮬레이터용, (36.119485, 128.3445734) 가져와서 날씨 정보 설정하기
        //setWeather(36, 128)
        //location_text.text = setXYtoAddr(36.119485, 128.3445734)
        // 사용자 위치(위도, 경도) 가져와서 날씨 정보 설정하기
        //requestLocation()

        //<------------------------------------화면 출력---------------------------------------->//
        // 초기 더미 데이터를 입력하는 부분
        val schedule_list_inner = mutableListOf<Schedule>()
        // 2022-05-28 실제 구글 캘린더 데이터가 연동되므로 더미 데이터의 입력은 불필요함
//        readDummyFile(schedule_list_inner)

        // 오늘 날짜 텍스트뷰 설정
        tvDate = findViewById(R.id.date)   // 오늘 날짜 텍스트뷰
        tvDate.text = SimpleDateFormat(
            "M월 d일",
            Locale.getDefault()
        ).format(Calendar.getInstance().time)


        // 리사이클러 뷰의 어댑터와 뷰모델을 인스턴스화
        model = ViewModelProvider(this).get(ScheduleViewModel::class.java)
        instance?.model = model

        // 리사이클러 뷰 호출
        member_schedule_adapter = ScheduleAdapter()
        schedule_list_recycler_view.adapter = member_schedule_adapter

        // 리사이클러 뷰에서 데이터가 변경되면 이를 반영하도록 함
        model.schedule_list.observe(this, Observer<MutableList<Schedule>> {
            member_schedule_adapter.notifyDataSetChanged()
        })

        // 뷰모델에 데이터를 삽입
        // 뷰모델에 데이터를 삽입하면 자동으로 UI에 변경사항이 반영됨
        // 2022-05-28 실제 구글 캘린더 데이터가 연동되므로 더미 데이터의 입력은 불필요함
//        member_schedule_adapter.schedule_list.addAll(schedule_list_inner)



        //weather_recyclerview = findViewById<View>(R.id.weather_list_recycler_view) as RecyclerView
        //weather_recyclerview.layoutManager = LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL, false)
    }














    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                        Weather API                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 날씨 정보 가져오기, 설정 ********************************************************************/
    private fun setWeather(nx : Int, ny : Int) {
        // 준비 단계 : base_date(발표 일자), base_time(발표 시각)
        // 현재 날짜, 시간 정보 가져오기
        val cal = Calendar.getInstance()
        base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time) // 날짜
        val time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시간
        // API 가져오기 적당하게 변환
        base_time = getTime(time)
        // 동네예보  API는 3시간마다 현재시간+4시간 뒤의 날씨 예보를 알려주기 때문에
        // 현재 시각이 00시가 넘었다면 어제 예보한 데이터를 가져와야함
        if (base_time >= "2300") {
            cal.add(Calendar.DATE, -1).toString()
            base_date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(cal.time)
        }
        // 날씨 정보 가져오기
        // (한 페이지 결과 수 = 60, 페이지 번호 = 1, 응답 자료 형식-"JSON", 발표 날짜, 발표 시각, 예보지점 좌표)
        var call = ApiObject.retrofitService.GetWeather(918, 1, "JSON",
            base_date, base_time, nx, ny)

        Log.d("x:" + base_date.toString(), "y:"+base_time.toString())
        // 비동기적으로 실행하기
        call.enqueue(object : retrofit2.Callback<WEATHER> {
            // 응답 성공 시
            @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
            override fun onResponse(call: Call<WEATHER>, response: Response<WEATHER>) {
                if (response.isSuccessful) {
                    // 날씨 정보 가져오기
                    val it: List<ITEM> = response.body()!!.response.body.items.item
                    weatheritem = it

                    // 배열 채우기
                    var index = 0
                    val totalCount = response.body()!!.response.body.totalCount

                    var nowTime = (
                            when(base_time){
                                "2300" -> (time+"00").toInt()-base_time.toInt()+2400-100
                                else -> (time+"00").toInt()-base_time.toInt()-100
                            })/100
                    val startcount = 12*nowTime
                    val count = 12*(9+nowTime)-1

                    for (i in startcount..count) {
                        if ((i%12)==0&&i!=startcount){
                            index++
                        }

                        when(it[i].category) {
                            "PTY" -> weatherArr[index].rainType = it[i].fcstValue     // 강수 형태
                            "REH" -> weatherArr[index].humidity = it[i].fcstValue     // 습도
                            "SKY" -> weatherArr[index].sky = it[i].fcstValue          // 하늘 상태
                            "TMP" -> weatherArr[index].temp = it[i].fcstValue         // 기온
                            else -> continue
                        }

                        weatherArr[index].Time = it[i].fcstTime
                    }


                    // 각 날짜 배열 시간 설정
//                    for (i in 0..6)
//                        weatherArr[i].Time = it[i].fcstTime

//                    var rainRatio = ""      // 강수 확률
//                    var rainType = ""       // 강수 형태
//                    var humidity = ""       // 습도
//                    var sky = ""            // 하늘 상태
//                    var temp = ""           // 기온
//                    var fcstTime = ""       // 예보 시각
//                    for (i in 0..9) {
//                        when (it[i].category) {
//                            "POP" -> rainRatio = it[i].fcstValue    // 강수 기온
//                            "PTY" -> rainType = it[i].fcstValue     // 강수 형태
//                            "REH" -> humidity = it[i].fcstValue     // 습도
//                            "SKY" -> sky = it[i].fcstValue          // 하늘 상태
//                            "TMP" -> temp = it[i].fcstValue         // 기온
//                            else -> continue
//                        }
//                    }


                    ////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////
                    //ScheduleAdapter에 전송(error, 전송이 되지 않아 NULL로 출력됨)

                    // 리사이클러 뷰에 데이터 연결
                    // 2022.06.02 상단 날씨 데이터가 출력되는 가로 리사이클러뷰
                    member_weather_adapter = WeatherScheduleAdapter(weatherArr)
                    member_weather_adapter.also { weather_list_recycler_view.adapter = it }
                    member_weather_adapter.notifyDataSetChanged()
//
//
//
                    model.weather_data.observe( this@MainActivity , Observer<Array<ModelWeather>> {
                        member_schedule_adapter.notifyDataSetChanged()
                    })

                    //intent.putExtra("weatherArr",weatherArr)
                    ////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////
                    ////////////////////////////////////////////////////////////////////////


                    // 2022-06-02 메인 화면 상단에 현재 날씨 표시
                    // 06.20 rain & snow 아이콘 추가
                    val mainTemp = findViewById<TextView>(R.id.temperature)    // 온도
                    val imgWeather = findViewById<ImageView>(R.id.imageView)    //아이콘
                    mainTemp.text = weatherArr[0].temp+"˚"
                    val sky = (
                            when(weatherArr[0].rainType) {

                                "0" -> weatherArr[0].sky ?: ""
                                else -> (weatherArr[0].rainType.toInt() + 10).toString()
                            }
                    )
                    imgWeather.setImageResource(Utils.decideWeatherIcon(sky))

                    // 2022-06-01 상단 현재 날씨 바 전체에 기온별로 색깔을 씌우기 위한 코드
                    // 2022-06-01 카드와 동일한 공식을 이용해서 전체 색깔을 바꿈.
                    // 2022-06-02 컬러코드 사용
//                    val today_weahter_bar = findViewById<LinearLayout>(R.id.today_weather_bar)
//                    val tempInt = weatherArr[0].temp.toInt()
//
//                    if (tempInt <= 15) {
//                        today_weahter_bar.setBackgroundColor(0x4D00299C)
//                    } else if (tempInt <= 12) {
//                        today_weahter_bar.setBackgroundColor(0x4D6182D6)
//                    } else if (tempInt <= 33) {
//                        today_weahter_bar.setBackgroundColor(0x4DBFBFBF)
//                    } else if (tempInt <= 35) {
//                        today_weahter_bar.setBackgroundColor(0x4DE86F62)
//                    } else if (tempInt > 35) {
//                        today_weahter_bar.setBackgroundColor(0x4DA41000)
//                    }


                    val today_weahter_bar = findViewById<LinearLayout>(R.id.main_background)
                    val tempInt = weatherArr[0].temp.toInt()

                    if (tempInt <= 13) {
                        today_weahter_bar.setBackgroundColor(resources.getColor(R.color.Clod_level2))
                    } else if (tempInt <= 15) {
                        today_weahter_bar.setBackgroundColor(resources.getColor(R.color.Clod_level1))
                    } else if (tempInt <= 33) {
                        today_weahter_bar.setBackgroundColor(resources.getColor(R.color.MainColor))
                        //today_weahter_bar.setBackground(getResources().getDrawable(R.drawable.background_main))
                    } else if (tempInt <= 35) {
                        today_weahter_bar.setBackgroundColor(resources.getColor(R.color.Hot_level1))
                    } else if (tempInt > 35) {
                        today_weahter_bar.setBackgroundColor(resources.getColor(R.color.Hot_level2))
                    }

                    // 토스트 띄우기
                    //Toast.makeText(applicationContext, it[0].fcstDate + ", " + it[0].fcstTime
                    //        + "의 날씨 정보입니다.", Toast.LENGTH_SHORT).show()
                }
            }

            // 응답 실패 시
            override fun onFailure(call: Call<WEATHER>, t: Throwable) {
//                val tvError = findViewById<TextView>(R.id.errormsg)
//                tvError.text = "api fail : " +  t.message.toString() + "\n 다시 시도해주세요."
//                tvError.visibility = View.VISIBLE
                Log.d("api fail", t.message.toString())
            }
        })
    }


    /** 예보 시간 반환 함수 ************************************************************************/
    fun getTime(time : String) : String {
        var result = ""
        when(time) {
            in "03".."05" -> result = "0200"    // 03~05
            in "06".."08" -> result = "0500"    // 06~08
            in "09".."11" -> result = "0800"    // 09~11
            in "12".."14" -> result = "1100"    // 12~14
            in "15".."17" -> result = "1400"    // 15~17
            in "18".."20" -> result = "1700"    // 18~20
            in "21".."23" -> result = "2000"    // 21~23
            else -> result = "2300"             // 00~02
        }
        return result
    }















    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                        Location                                          //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 사용자 현재 위치의(위도, 경도) 격자 좌표로 변환하여 해당 위치의 날씨정보 설정하기 ********************/
    @SuppressLint("MissingPermission")
    private fun requestLocation() {
/*
        val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        var location_text : TextView = findViewById<View>(R.id.location) as TextView

        try {
            // 사용자의 현재 위치 요청
            val locationRequest = LocationRequest.create()
            locationRequest.run {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                interval = 60 * 1000    // 요청 간격(1초)
            }
            val locationCallback = object : LocationCallback() {
                // 요청 결과
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.let {
                        for (location in it.locations) {
                            // 현재 위치(위도, 경도)를 격자 좌표로 변환
                            curPoint = dfs_xy_conv(location.latitude, location.longitude)

                            // 오늘 날짜 텍스트뷰 설정
                            tvDate.text = SimpleDateFormat("MM월 dd일",
                                Locale.getDefault()).format(Calendar.getInstance().time) + "날씨"
                            // nx, ny지점의 날씨 가져와서 설정하기
                            setWeather(curPoint!!.x.toDouble(), curPoint!!.y.toDouble())
                            location_text.text = setXYtoAddr(curPoint!!.x.toDouble(), curPoint!!.y.toDouble())
                        }
                    }
                }
            }

            // 사용자 위치 실시간으로 감지
            locationClient.requestLocationUpdates(locationRequest, locationCallback,
                Looper.myLooper()!!
            )
        } catch (e : SecurityException) {
            e.printStackTrace()
        }
*/
        var mGeocoder: Geocoder = Geocoder(applicationContext, Locale.KOREAN)
        var mResultList: List<Address>? = null
        val locationProviderClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)

        val locationClient = LocationServices.getFusedLocationProviderClient(this@MainActivity)
        var location_text : TextView = findViewById<View>(R.id.location_text) as TextView

        try {
            val locationRequest = LocationRequest.create()
            //2022.06.04 위치 갱신 취소-입력 주소와 충돌
            //2022.06.12 복구
//                locationRequest.run {
//                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//                    interval = 60 * 1000    // 요청 간격(1초)
//                }
            locationProviderClient.lastLocation.addOnSuccessListener {
                var latitude = it.latitude
                var longitude = it.longitude

                try {
                    mResultList = mGeocoder.getFromLocation(latitude, longitude, 1)
                    cur_x=latitude
                    cur_y=longitude
                    saveData()
                    //println("위치 정보 받아오기 성공")
                } catch (e: Exception) {
                    e.printStackTrace()
                    location_text.text = "좌표를 변환하지 못했습니다."
                }

                //날씨 정보 출력
                val locationCallback = object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        locationResult.let {
                            for (location in it.locations) {
                                // 현재 위치(위도, 경도)를 격자 좌표로 변환
                                curPoint = dfs_xy_conv(location.latitude, location.longitude)
                                setWeather(curPoint!!.x, curPoint!!.y)

//                                // 오늘 날짜 텍스트뷰 설정
//                                tvDate = findViewById(R.id.date)   // 오늘 날짜 텍스트뷰
//                                tvDate.text = SimpleDateFormat(
//                                    "M월 d일",
//                                    Locale.getDefault()
//                                ).format(Calendar.getInstance().time)

                                var currentAddr = setXYtoAddr(latitude, longitude).split(' ')
                                var addr = ""
                                for(i in 3..currentAddr.size)
                                    addr += currentAddr[i-1] + " "
                                location_text.text = addr

                            }
                        }
                    }
                }
                // 사용자 위치 실시간으로 감지
                locationClient.requestLocationUpdates(locationRequest, locationCallback,
                    Looper.myLooper()!!)
            }
        } catch (e: Exception) {
            location_text.text = "위치 정보를 받아오지 못했습니다."
        }
    }


    /** Base Time 설정 ***********************************************************************/
    fun getBaseTime(h : String, m : String) : String {
        var result = ""

        // 45분 전이면
        if (m.toInt() < 45) {
            // 0시면 2330
            if (h == "00") result = "2330"
            // 아니면 1시간 전 날씨 정보 부르기
            else {
                var resultH = h.toInt() - 1
                // 1자리면 0 붙여서 2자리로 만들기
                if (resultH < 10) result = "0" + resultH + "30"
                // 2자리면 그대로
                else result = resultH.toString() + "30"
            }
        }
        // 45분 이후면 바로 정보 받아오기
        else result = h + "30"

        return result
    }


    /** (위도, 경도)-->(기상정 좌표계) **********************************************************/
    fun dfs_xy_conv(v1: Double, v2: Double) : Point {
        val RE = 6371.00877     // 지구 반경(km)
        val GRID = 5.0          // 격자 간격(km)
        val SLAT1 = 30.0        // 투영 위도1(degree)
        val SLAT2 = 60.0        // 투영 위도2(degree)
        val OLON = 126.0        // 기준점 경도(degree)
        val OLAT = 38.0         // 기준점 위도(degree)
        val XO = 43             // 기준점 X좌표(GRID)
        val YO = 136            // 기준점 Y좌표(GRID)
        val DEGRAD = Math.PI / 180.0
        val re = RE / GRID
        val slat1 = SLAT1 * DEGRAD
        val slat2 = SLAT2 * DEGRAD
        val olon = OLON * DEGRAD
        val olat = OLAT * DEGRAD

        var sn = Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) / Math.log(sn)
        var sf = Math.tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = Math.pow(sf, sn) * Math.cos(slat1) / sn
        var ro = Math.tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / Math.pow(ro, sn)

        var ra = Math.tan(Math.PI * 0.25 + (v1) * DEGRAD * 0.5)
        ra = re * sf / Math.pow(ra, sn)
        var theta = v2 * DEGRAD - olon
        if (theta > Math.PI) theta -= 2.0 * Math.PI
        if (theta < -Math.PI) theta += 2.0 * Math.PI
        theta *= sn

        val x = (ra * Math.sin(theta) + XO + 0.5).toInt()
        val y = (ro - ra * Math.cos(theta) + YO + 0.5).toInt()

        return Point(x, y)
    }


























    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                    NotificationTime                                      //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** NotificationTime의 main *******************************************************************/
    fun mainNotificationTime() {
//        btnMainToSetting = findViewById(R.id.btn_main_to_second)
//
//        btnMainToSetting.setOnClickListener {
//            val intent = Intent(this, SettingActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
    }









    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Google Calendar                                     //
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
    fun mainGoogleCalendar() {
//        mAddCalendarButton = findViewById<View>(R.id.button_main_add_calendar) as Button
//        mAddEventButton = findViewById<View>(R.id.button_main_add_event) as Button
//        mGetEventButton = findViewById<View>(R.id.button_main_get_event) as Button
//        mStatusText = findViewById<View>(R.id.textview_main_result) as TextView
//        mResultText = findViewById<View>(R.id.textview_main_status) as TextView


        //동작 테스트
//        mAddCalendarButton!!.setOnClickListener {
//            mAddCalendarButton!!.isEnabled = false
//            mStatusText!!.text = ""
//            mID = 1 //캘린더 생성
//            resultsFromApi
//            mAddCalendarButton!!.isEnabled = true
//        }
//        mAddEventButton!!.setOnClickListener {
//            mAddEventButton!!.isEnabled = false
//            mStatusText!!.text = ""
//            mID = 2 //이벤트 생성
//            resultsFromApi
//            mAddEventButton!!.isEnabled = true
//        }
//       mGetEventButton!!.setOnClickListener {
//           mGetEventButton!!.isEnabled = false
//            mStatusText!!.text = ""
//           mID = 3 //이벤트 가져오기
//           resultsFromApi
//           mGetEventButton!!.isEnabled = true
//       }


        // Google  API의 호출 결과를 표시하는 TextView를 준비
//        mResultText!!.isVerticalScrollBarEnabled = true
//        mResultText!!.movementMethod = ScrollingMovementMethod()
//        mStatusText!!.isVerticalScrollBarEnabled = true
//        mStatusText!!.movementMethod = ScrollingMovementMethod()
//        mStatusText!!.text = "버튼을 눌러 테스트를 진행하세요."


        // Google Calendar API 호출중에 표시되는 ProgressDialog
        mProgress = ProgressDialog(this)
        mProgress!!.setMessage("Google Calendar API 호출 중입니다.")


        // Google Calendar API 사용하기 위해 필요한 인증 초기화( 자격 증명 credentials, 서비스 객체 )
        // OAuth 2.0를 사용하여 구글 계정 선택 및 인증하기 위한 준비
        mCredential = GoogleAccountCredential.usingOAuth2(
            applicationContext,
            Arrays.asList(*SCOPES)
        ).setBackOff(ExponentialBackOff()) // I/O 예외 상황을 대비해서 백오프 정책 사용

        //06.19 google calendar list 불러오기
        resultsFromApi
    }







    //////////////////////////////////////////////////////////////////////////////////////////////
    //                              Check Google Play Services                                  //
    //////////////////////////////////////////////////////////////////////////////////////////////

    // 다음 사전 조건을 모두 만족해야 Google Calendar API를 사용할 수 있다.
    //
    // 1. Google Play Services 설치
    // 2. 유효한 구글 계정 선택
    // 3. 인터넷 사용 가능
    //
    // 하나라도 만족하지 않으면 해당 사항을 사용자에게 알림.
    /** Google Calendar API 사용 조건 ************************************************************/
    private val resultsFromApi: String?
        private get() {
            // 1. Google Play Services를 사용할 수 없는 경우
            if (!isGooglePlayServicesAvailable){
                acquireGooglePlayServices()
            }
            // 2. 유효한 Google 계정이 선택되어 있지 않은 경우
//            else if (mCredential!!.selectedAccountName == null){
            if (mCredential?.selectedAccountName == null){
                chooseAccount()
            }
            // 3. 인터넷을 사용할 수 없는 경우
//            else if (!isDeviceOnline) {
            if (!isDeviceOnline) {
//                mStatusText!!.text = "No network connection available."
            }
            //MakeRequestTask(this, mCredential).execute()
            // Google Calendar API 호출
            MakeRequestTask(MainActivity(), mCredential).execute()

            return null
        }




    /** Google Play Services 설치 확인 ***********************************************************/
    private val isGooglePlayServicesAvailable: Boolean
        private get() {
            val apiAvailability = GoogleApiAvailability.getInstance()
            val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
            return connectionStatusCode == ConnectionResult.SUCCESS
        }




    //Google Play Services 업데이트로 해결가능하다면 사용자가 최신 버전으로 업데이트하도록 요청
    /** Google Play Services 업데이트 요청 메세지 **************************************************/
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(this)
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }




    /** Google Play Services 설치 안되었거나/오래된 버전인 경우 메세지 ********************************/
    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this@MainActivity,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog!!.show()
    }




    //Google Calendar API의 자격 증명( credentials ) 에 사용할 구글 계정을 설정한다.
    //
    //전에 사용자가 구글 계정을 선택한 적이 없다면 다이얼로그에서 사용자를 선택하도록 한다.
    //GET_ACCOUNTS 퍼미션이 필요하다.
    /** 구글 계정 설정 ***************************************************************************/
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    private fun chooseAccount() {

        // GET_ACCOUNTS 권한을 가지고 있을 때
        if (EasyPermissions.hasPermissions(this, Manifest.permission.GET_ACCOUNTS)) {
            // SharedPreferences에서 저장된 Google 계정 이름을 가져온다.
            val accountName = getPreferences(MODE_PRIVATE)
                .getString(PREF_ACCOUNT_NAME, null)

            // 사용자가 구글 계정을 선택할 수 있는 다이얼로그를 보여준다.
            if (accountName == null) {
                startActivityForResult(
                    mCredential?.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER)
                //resultsFromApi
            }
            // 선택된 구글 계정 이름으로 설정한다.
            if (accountName != null) {
                mCredential?.selectedAccountName = accountName
            }
        }
        // GET_ACCOUNTS 권한을 가지고 있지 않다면
        else {
            // 사용자에게 GET_ACCOUNTS 권한을 요구하는 다이얼로그를 보여준다.(주소록 권한 요청함)
            EasyPermissions.requestPermissions(
                (this as Activity),
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS
            )
        }
    }



    //구글 플레이 서비스 업데이트 다이얼로그, 구글 계정 선택 다이얼로그, 인증 다이얼로그에서 되돌아올때 호출된다.
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(
        requestCode: Int,  // onActivityResult가 호출되었을 때 요청 코드로 요청을 구분
        resultCode: Int,  // 요청에 대한 결과 코드
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != RESULT_OK) {
//                mStatusText!!.text =
//                    (" 앱을 실행시키려면 구글 플레이 서비스가 필요합니다."
//                            + "구글 플레이 서비스를 설치 후 다시 실행하세요.")
            }
            else {
                resultsFromApi
            }
            REQUEST_ACCOUNT_PICKER ->
                if (resultCode == RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        val settings = getPreferences(MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
                        editor.apply()
                        mCredential!!.selectedAccountName = accountName
                        resultsFromApi
                    }
                }
            REQUEST_AUTHORIZATION -> if (resultCode == RESULT_OK) {
                resultsFromApi
            }
        }
    }



    //Android 6.0 (API 23) 이상에서 런타임 권한 요청시 결과를 리턴받음
    override fun onRequestPermissionsResult(
        requestCode: Int,  //requestPermissions(android.app.Activity, String, int, String[])에서 전달된 요청 코드
        permissions: Array<String>,  // 요청한 퍼미션
        grantResults: IntArray // 퍼미션 처리 결과. PERMISSION_GRANTED 또는 PERMISSION_DENIED
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 승인한 경우 호출된다.
    //override fun onPermissionsGranted(requestCode: Int, requestPermissionList: List<String>) {
    //
    //      아무일도 하지 않음
    //}


    //EasyPermissions 라이브러리를 사용하여 요청한 권한을 사용자가 거부한 경우 호출된다.
    //override fun onPermissionsDenied(requestCode: Int, requestPermissionList: List<String>) {
    //
    //     아무일도 하지 않음
    //}


    //안드로이드 디바이스가 인터넷 연결되어 있는지 확인한다. 연결되어 있다면 True 리턴, 아니면 False 리턴
    private val isDeviceOnline: Boolean
        @SuppressLint("MissingPermission") private get() {
            val connMgr = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connMgr.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }







    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                      Use Calendar                                        //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 캘린더 이름에 대응하는 캘린더 ID 리턴 ********************************************************/
    private fun getCalendarID(calendarTitle: String): String? {
        var id: String? = null

        // Iterate through entries in calendar list
        var pageToken: String? = null
        do {
            var calendarList: CalendarList? = null
            try {
                calendarList = google_service!!.calendarList().list().setPageToken(pageToken).execute()
            } catch (e: UserRecoverableAuthIOException) {
                startActivityForResult(e.intent, REQUEST_AUTHORIZATION)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            val items = calendarList!!.items
            for (calendarListEntry in items) {
                if (calendarListEntry.summary.toString() == calendarTitle) {
                    id = calendarListEntry.id.toString()
                }
            }
            pageToken = calendarList.nextPageToken
        } while (pageToken != null)
        return id
    }


    /** Google Calendar API 호출 ****************************************************************/
    private inner class MakeRequestTask(
        private val mActivity: MainActivity,
        credential: GoogleAccountCredential?
    ) :
        AsyncTask<Void?, Void?, String?>() {
        private var mLastError: Exception? = null
        var eventStrings: MutableList<String?> = ArrayList()
        override fun onPreExecute() {
            // mStatusText.setText("");
            mProgress?.show()
//            mStatusText?.text = "데이터 가져오는 중..."
//            mResultText?.text = ""
        }


        /** 백그라운드에서 Google Calendar API 호출 처리 *******************************************/
        override fun doInBackground(vararg p0: Void?): String? {
            try {
//                when (mID) {
//                    1 -> {
//                        return createCalendar()
//                    }
//                    2 -> {
//                        return addEvent()
//                    }
//                    3 -> {
//                        return event
                       event
//                    }
//                }
            }
            catch (e: Exception) {
                mLastError = e
                cancel(true)
                return null
            }
            return null
        }// 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용//"primary")

        //.setTimeMin(now)
        /** CalendarTitle 이름의 캘린더에서 10개의 이벤트를 가져와 리턴 *******************************/
        @get:Throws(IOException::class)
        private val event: String
            private get() {
                val now = DateTime(System.currentTimeMillis())
                val calendarID = getCalendarID("CalendarTitle")
                    ?: return "캘린더를 먼저 생성하세요."
                val events = google_service!!.events().list(calendarID) //"primary")
                    .setMaxResults(10)
                    .setTimeMin(now)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .execute()
                val items = events.items
                val countschedule = findViewById<TextView>(R.id.datecnt)

                // 2022-06-07 중복 데이터 제거 위치 변경
                model.schedule_list.value?.clear()
                    //06.19 UI 변경 Error
                    runOnUiThread(Runnable{
                        for (event in items) {
                            var start = event.start.dateTime
                            if (start == null) {

                                // 모든 이벤트가 시작 시간을 갖고 있지는 않다. 그런 경우 시작 날짜만 사용
                                start = event.start.date
                            }
                            eventStrings.add(String.format("%s  (%s)\n", event.summary, start))
                            // 2022-05-28 중복된 데이터가 생기는 것을 방지
                            // 2022-056-07 중복 데이터 제거 위치 변경
//                    model.schedule_list.value?.clear()

                            // 2022-05-26 구글 캘린더 데이터를 실제 레이아웃에 연결
                            model.schedule_list.value?.add(Schedule(
                                uid = if (event.id != null) event.id else "",
                                created = if (event.created != null) event.created.toString() else "",
                                description = if (event.description != null) event.description else "",
                                location = if (event.location != null) event.location else "",
                                summary = if (event.summary != null) event.summary else "",
                                end = if (event.end != null && event.end.dateTime != null) event.end.dateTime.toString() else "",
                                begin = if (event.start != null && event.start.dateTime != null) event.start.dateTime.toString() else "",
                                dtstart = if (event.start != null && event.start.date != null) event.start.date.toString() else "",
                                dtend = if (event.end != null && event.end.date != null) event.end.date.toString() else "",
                                weather = ModelWeather(),
                            ))

                        }

                        // 2022-05-26 데이터가 변경되었음을 알려 갱신을 유도함
                        member_schedule_adapter.notifyDataSetChanged()


                        countschedule.text = eventStrings.size.toString() + "개"
                    })

                //return eventStrings.size.toString() + "개의 데이터를 가져왔습니다."
                return "가져온 데이터 \n" + eventStrings.toString()
            }


        /** 선택되어 있는 Google 계정에 새 캘린더를 추가한다. *****************************************/
        @Throws(IOException::class)
        private fun createCalendar(): String {
            val ids = getCalendarID("CalendarTitle")
            if (ids != null) {
                return "이미 캘린더가 생성되어 있습니다. "
            }

            // 새로운 캘린더 생성
            val calendar = com.google.api.services.calendar.model.Calendar()

            // 캘린더의 제목 설정
            calendar.summary = "CalendarTitle"


            // 캘린더의 시간대 설정
            calendar.timeZone = "Asia/Seoul"

            // 구글 캘린더에 새로 만든 캘린더를 추가
            val createdCalendar = google_service!!.calendars().insert(calendar).execute()

            // 추가한 캘린더의 ID를 가져옴.
            val calendarId = createdCalendar.id


            // 구글 캘린더의 캘린더 목록에서 새로 만든 캘린더를 검색
            val calendarListEntry = google_service!!.calendarList()[calendarId].execute()

            // 캘린더의 배경색을 파란색으로 표시  RGB
            calendarListEntry.backgroundColor = "#0000ff"

            // 변경한 내용을 구글 캘린더에 반영
            val updatedCalendarListEntry = google_service!!.calendarList()
                .update(calendarListEntry.id, calendarListEntry)
                .setColorRgbFormat(true)
                .execute()

            // 새로 추가한 캘린더의 ID를 리턴
            return "캘린더가 생성되었습니다."
        }

        override fun onPostExecute(output: String?) {
            mProgress!!.hide()
//            mStatusText!!.text = output
//            if (mID == 3) mResultText!!.text = TextUtils.join("\n\n", eventStrings)
        }

        /** 요청에 실패하면 에러메시지를 출력한다. ***************************************************/
        @SuppressLint("SetTextI18n")
        override fun onCancelled() {
            mProgress!!.hide()
            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                        (mLastError as GooglePlayServicesAvailabilityIOException)
                            .connectionStatusCode
                    )
                }
                else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                        (mLastError as UserRecoverableAuthIOException).intent,
                        REQUEST_AUTHORIZATION
                    )
                }
                else {
//                    var errormsg = findViewById<TextView>(R.id.errormsg)
//                    errormsg.text = """
//                        MakeRequestTask The following error occurred:
//                        ${mLastError!!.message}
//                        """.trimIndent()
                }
            }
            else {
//                mStatusText!!.text = "요청 취소됨."
            }
        }

        /** 일정 추가 **************************************************************************/
        private fun addEvent(): String {
            val calendarID = getCalendarID("CalendarTitle") ?: return "캘린더를 먼저 생성하세요."
            var event =
                Event()
                    .setSummary("구글 캘린더 테스트")
                    .setLocation("서울시")
                    .setDescription("캘린더에 이벤트 추가하는 것을 테스트합니다.")
            val calander: java.util.Calendar
            calander = java.util.Calendar.getInstance()
            val simpledateformat: SimpleDateFormat
            //simpledateformat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ", Locale.KOREA);
            // Z에 대응하여 +0900이 입력되어 문제 생겨 수작업으로 입력
            simpledateformat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+09:00", Locale.KOREA)
            val datetime = simpledateformat.format(calander.time)
            val startDateTime =
                DateTime(datetime)
            val start = EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Seoul")
            event.start = start
            Log.d("@@@", datetime)
            val endDateTime =
                DateTime(datetime)
            val end = EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Seoul")
            event.end = end

            //String[] recurrence = new String[]{"RRULE:FREQ=DAILY;COUNT=2"};
            //event.setRecurrence(Arrays.asList(recurrence));
            try {
                event = google_service!!.events().insert(calendarID, event).execute()
            }
            catch (e: Exception) {
                e.printStackTrace()
                Log.e("Exception", "Exception : $e")
            }
            System.out.printf("Event created: %s\n", event.htmlLink)
            Log.e("Event", "created : " + event.htmlLink)
            return "created : " + event.htmlLink
        }

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory: JsonFactory = JacksonFactory.getDefaultInstance()
            google_service = com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
                .setApplicationName("Google Calendar API Android Quickstart")
                .build()
        }
    }


















    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                       Notification                                       //
    //////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////////////   //////////////////////////////////////////////
    /////////////////////////////////////                   //////////////////////////////////////
    ////////////////////////////////////////             /////////////////////////////////////////
    ////////////////////////////////////////////     /////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** Notification의 main *********************************************************************/
    fun mainNotification() {
        //clearExistingNotifications(NOTIFICATION_ID)
        createNotificationChannel(
            this, NotificationManagerCompat.IMPORTANCE_DEFAULT, false,
            getString(R.string.app_name), "App notification channel"
        )

        var button = findViewById<TextView>(R.id.alarmTime)
            button.setOnClickListener {
                getTime(button, this)
        }


        // 2022-06-09 Setting Activity에서 가져온 알람 설정 여부 저장 및 이벤트 리스너 설정 코드
        m_switch_alarm_1 = findViewById<Switch>(R.id.switchAlarm1) as CompoundButton
        m_switch_alarm_1!!.isChecked =
            PreferenceHelper.getBoolean(applicationContext, Constants.SHARED_PREF_NOTIFICATION_KEY_A)
        m_switch_alarm_1!!.setOnCheckedChangeListener { buttonView, isChecked ->
            // 2022-06-21 스위치를 켰을 때 위젯을 사용할 수 있도록 변수 할당
            alarmTime = findViewById(R.id.alarmTime)
            // 2022-06-21 알람 설정 및 모델이 제대로 동작하도록 하단 코드 추가
            instance?.alarmTime = findViewById(R.id.alarmTime)
            instance?.model = model
            instance?.model?.schedule_list = model.schedule_list
            instance?.model?.weather_data = model.weather_data
            if (isChecked) {
                val isChannelCreated: Boolean = NotificationHelper.isNotificationChannelCreated(
                    applicationContext
                )
                if (isChannelCreated) {
                    PreferenceHelper.setBoolean(
                        applicationContext,
                        Constants.SHARED_PREF_NOTIFICATION_KEY_A,
                        true
                    )
                    NotificationHelper.setScheduledNotification(WorkManager.getInstance(applicationContext))
                } else {
                    NotificationHelper.createNotificationChannel(applicationContext)
                }
            } else {
                PreferenceHelper.setBoolean(
                    applicationContext,
                    Constants.SHARED_PREF_NOTIFICATION_KEY_A,
                    false
                )
                WorkManager.getInstance(applicationContext).cancelAllWork()
            }
        }

//        m_switch_alarm_2 = findViewById<Switch>(R.id.switchAlarm2) as CompoundButton
//        m_switch_alarm_2!!.isChecked =
//            PreferenceHelper.getBoolean(applicationContext, Constants.SHARED_PREF_NOTIFICATION_KEY_B)
//        m_switch_alarm_2!!.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                val isChannelCreated: Boolean = NotificationHelper.isNotificationChannelCreated(
//                    applicationContext
//                )
//                if (isChannelCreated) {
//                    PreferenceHelper.setBoolean(
//                        applicationContext,
//                        Constants.SHARED_PREF_NOTIFICATION_KEY_B,
//                        true
//                    )
//                    NotificationHelper.setScheduledNotification(WorkManager.getInstance(applicationContext))
//                } else {
//                    NotificationHelper.createNotificationChannel(applicationContext)
//                }
//            } else {
//                PreferenceHelper.setBoolean(
//                    applicationContext,
//                    Constants.SHARED_PREF_NOTIFICATION_KEY_B,
//                    false
//                )
//                WorkManager.getInstance(applicationContext).cancelAllWork()
//            }
//        }


        //showNotification()
    }

    fun getTime(button: TextView, context: Context){

        val cal = Calendar.getInstance()
        var timetxt = findViewById<TextView>(R.id.alarmTime)

        val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
            cal.set(Calendar.HOUR_OF_DAY, hour)
            cal.set(Calendar.MINUTE, minute)

            timetxt.text = SimpleDateFormat("HH:mm").format(cal.time)
            saveData()
        }

        TimePickerDialog(context, timeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()

    }




    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                          Channel                                         //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 이미 존재하는 Notification 삭제 ************************************************************/
    private fun clearExistingNotifications(notificationId: Int) {
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)

    }

    /** 채널 생성 함수 ****************************************************************************/
    private fun createNotificationChannel(context: Context, importance: Int, showBadge: Boolean,
                                          name: String, description: String) {
        //// 오레오 버전 이상부터는 (사실상 현재 쓰이는 폰 대부분이) 채널 정보 필요
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        //    val channelId = "${context.packageName}-$name"
        //    val channel = NotificationChannel(channelId, name, importance)
        //    channel.description = description
        //    channel.setShowBadge(showBadge)
        //
        //    //만든 채널 정보를 시스템에 등록
        //    val notificationManager: NotificationManager =
        //        getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        //    notificationManager.createNotificationChannel(channel)
        //}






        //var button = findViewById(R.id.button) as Button
        //
        //// Set Intent
        // 2022-06-09 예제 코드 주석 처리
//        val resultIntent = Intent(this, SettingActivity::class.java)
//        //// Create Task Stack Builder
//        val resultPendingIntent : PendingIntent? = TaskStackBuilder.create(this).run {
//            addNextIntentWithParentStack(resultIntent)
//            // 2022-05-28 FLAG_MUTABLE이나 FLAG_IMMUTABLE을 붙여줘야함. 그렇지 않을 경우 안드로이드 특정 버전 이상에서 크래시 발생
//            getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
//        }
//
//        //button.setOnClickListener {
//        val alarm_number = (1..10).random()				//Set alarm number randomically
//        var builder = NotificationCompat.Builder(this, "MY_channel")
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            .setContentTitle("알림 제목"+alarm_number)		//Set Alarm Title by adding alarm number
//            .setContentText("알림 내용")
//            .setContentIntent(resultPendingIntent)		//Set Intent when alarm is pressed. To do so, New Activity(SettingActivity) is started.
//            .setAutoCancel(true)						//Automatically erase alarm when alarm is pressed
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            val channel_id = "MY_channel"
//            val channel_name = "채널이름"
//            val descriptionText = "설명글"
//            val importance = NotificationManager.IMPORTANCE_DEFAULT
//            val channel = NotificationChannel(channel_id, channel_name, importance)
//            //.apply { description = descriptionText }
//
//            val notificationManager: NotificationManager =
//                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//
////            notificationManager.notify(alarm_number, builder.build())
//        }


        // 2022-06-02 예제 코드 주석처리.
        /////////////////////////////////////////////////////////////////////////
        /* Group A */

//        var alarm_number01 = (1..10).random()
//        val SUMMARY_ID_01 = 0
//        val GROUP_KEY_A = "com.jihakjaengi.example.A"
//        var builder = NotificationCompat.Builder(this, "MY_Channel")
//            .setSmallIcon(R.drawable.sunny)
//            .setContentTitle("알림 제목"+alarm_number01)
//            .setContentText("알림 내용")
//            .setAutoCancel(true)
//            .setGroup(GROUP_KEY_A)
//            .build()
//
//        val summaryNotification01 = NotificationCompat.Builder(this@MainActivity, "MY_Channel")
//            .setContentTitle("0523")
//            //set content text to support devices running API level < 24
//            .setContentText("Two new messages")
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            //build summary info into InboxStyle template
//            .setStyle(NotificationCompat.InboxStyle()
//                .addLine("Alex Faarborg Check this out")
//                .addLine("Jeff Chang Launch Party")
//                .setBigContentTitle("2 new messages")
//                .setSummaryText("3 Schedules"))
//            //specify which group this notification belongs to
//            .setGroup(GROUP_KEY_A)
//            //set this notification as the summary for the group
//            .setGroupSummary(true)
//            .build()
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오 버전 이후에는 알림을 받을 때 채널이 필요
//            val channel_id = "MY_Channel" // 알림을 받을 채널 id 설정
//            val channel_name = "채널이름" // 채널 이름 설정
//            val descriptionText = "설명글" // 채널 설명글 설정
//            val importance = NotificationManager.IMPORTANCE_DEFAULT // 알림 우선순위 설정
//            val channel = NotificationChannel(channel_id, channel_name, importance).apply {
//                //description = descriptionText
//            }
//
//            // 만든 채널 정보를 시스템에 등록
//            val notificationManager: NotificationManager =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
//
//            NotificationManagerCompat.from(this).apply {
//                notify(alarm_number01, builder)
//                //notify(emailNotificationId2, newMessageNotification2)
//                notify(SUMMARY_ID_01, summaryNotification01)
//        }
//
//            //notificationManager.notify(alarm_number, builder)
//    }

        //////////////////////////////////////////////////////
        /* Group B */

        val GROUP_KEY_B = "com.example.dayday.B"
        val SUMMARY_ID_02 = 1
        val alarm_number21 = (11..20).random()
        val alarm_number22 = (11..20).random()
        val alarm_number23 = (11..20).random()

        ////////////////////////////////////////////////////
        /* builder pattern */

//        var builder21 = NotificationCompat.Builder(this, "MY_Channel")
//            .setSmallIcon(R.drawable.cloud)
//            .setContentTitle("웹 프로그래밍 강의")
//            .setContentText("맑음 17℃")
//            .setAutoCancel(true)
//            .setGroup(GROUP_KEY_B)
//            .build()
//
//        var builder22 = NotificationCompat.Builder(this, "MY_Channel")
//            .setSmallIcon(R.drawable.cloud)
//            .setContentTitle("엣지프로젝트 강의")
//            .setContentText("맑음 17℃")
//            .setAutoCancel(true)
//            .setGroup(GROUP_KEY_B)
//            .build()
//
//        var builder23 = NotificationCompat.Builder(this, "MY_Channel")
//            .setSmallIcon(R.drawable.cloud)
//            .setContentTitle("창의설계프로젝트 강의")
//            .setContentText("맑음 17℃")
//            .setAutoCancel(true)
//            .setGroup(GROUP_KEY_B)
//            .build()
//
//        /* summary Notification */
//        val summaryNotification02 = NotificationCompat.Builder(this@MainActivity, "MY_Channel")
//            .setContentTitle("0524")
//            //set content text to support devices running API level < 24
//            .setContentText("Two new messages")
//            .setSmallIcon(R.drawable.cloud)
//            //build summary info into InboxStyle template
//            .setStyle(NotificationCompat.InboxStyle()
//                .addLine("Alex Faarborg Check this out")
//                .addLine("Jeff Chang Launch Party")
//                .setBigContentTitle("2 new messages")
//                .setSummaryText("4 Schedules"))
//            //specify which group this notification belongs to
//            .setGroup(GROUP_KEY_B)
//            //set this notification as the summary for the group
//            .setGroupSummary(true)
//            .build()
//
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 오레오 버전 이후에는 알림을 받을 때 채널이 필요
            val channel_id = "MY_Channel" // 알림을 받을 채널 id 설정
            val channel_name = "채널이름" // 채널 이름 설정
            val descriptionText = "설명글" // 채널 설명글 설정
            val importance = NotificationManager.IMPORTANCE_DEFAULT // 알림 우선순위 설정
            val channel = NotificationChannel(channel_id, channel_name, importance).apply {
                //description = descriptionText
            }

//             2022-06-01 스케쥴을 읽어서 알람으로 내보내므로 제거
            // 만든 채널 정보를 시스템에 등록
//        val notificationManager: NotificationManager  =
//                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            notificationManager.createNotificationChannel(channel)
////
////
////            /* 자식 알람 + 부모 알람 apply */
//
//            val aaaa = mutableListOf<Notification>()
//
//            aaaa.add(builder21)
//            aaaa.add(builder22)
//            aaaa.add(builder23)
//
//            NotificationManagerCompat.from(this).apply {
//                var i = 0
//                for(x in aaaa) {
//                    notify(i, x)
//                    i++
//                }
//                //notify(emailNotificationId2, newMessageNotification2)
//                notify(SUMMARY_ID_02, summaryNotification02)
//            }

            // 2022-06-01 저장된 일정을 알림으로 출력
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)

            val weather_iter = model.weather_data.value?.iterator()
            val notification_list = mutableListOf<Notification>()
            var i = 0
            for (i in 0 until (model.schedule_list.value!!.size)) {

                var schedule = model.schedule_list.value!![i]
                val weather_model = weather_iter?.next()

                //06.20 rain 타입 추가
                //sky: 0...
                //rainType: 10 ...
                val sky = (
                        when(weather_model?.rainType) {

                            "0" -> weather_model?.sky ?: ""
                            else -> (weather_model?.rainType!!.toInt() + 10).toString()
                        }
                )
                var sky_string = ""
                if (sky == "1") {
                    sky_string = "맑음"
                } else if (sky == "3") {
                    sky_string = "구름 많음"
                } else if (sky == "4") {
                    sky_string = "흐림"
                } else if (sky == "11"){
                    sky_string = "비"
                } else{

                }

                if (i == 0) {
                    var builder_schedule_1 = NotificationCompat.Builder(this, "MY_Channel")
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(true)
                        .build()

                    var builder_schedule_2 = NotificationCompat.Builder(this, "MY_Channel")
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
                        .build()

                    notification_list.add(builder_schedule_1)
                    notification_list.add(builder_schedule_2)
                }
                else {
                    var builder_schedule = NotificationCompat.Builder(this, "MY_Channel")
                        .setSmallIcon(Utils.decideWeatherIcon(sky))
                        .setContentTitle(schedule.summary)
                        .setContentText(sky_string + " " + (weather_model?.temp ?: "") + "℃")
                        .setAutoCancel(true)
                        .setGroup(GROUP_KEY_B)
                        .setGroupSummary(false)
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
            NotificationManagerCompat.from(this).apply {
                for (notifi in notification_list) {
                    notify(i, notifi)
                    i++
                }
            }
        }




//            if (notification_list.isEmpty() == false) {
//                notificationManager.notify(i, notification_list.last())
//            }

        /* 자식 알람 + 부모 알람 apply */


        //notificationManager.notify(alarm_number21, builder21)
    }


    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                           Print                                          //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** 알림 출력 함수 ****************************************************************************/
//    private fun showNotification() {
//        //val mLargeIconForNoti =
//        //    BitmapFactory.decodeFile(getFilesDir().path + "/test.png");
//        var notification_builder = NotificationCompat.Builder(this, "MY_channel")
//            .setSmallIcon(R.drawable.ic_launcher_background)
//            //    .setLargeIcon(mLargeIconForNoti)
//            .setContentTitle("알림 제목 입력")
//            .setContentText("알림 내용 입력")
//        // 2022-05-28 실제 데이터로 연동하므로 주석 처리함
////        val schedule_list = mutableListOf<Schedule>()
//        val notificationManager: NotificationManager  =
//            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
//        // 2022-05-28 실제 데이터로 연동하므로 주석 처리함
////        readDummyFile(schedule_list)
//        notificationManager.notify(Random.nextInt(), notification_builder.build())
//        //리스트를 순회하면서 알림 출력
//
//        // 뷰모델 자동 추가 실험 코드
//        // TODO : 삭제해도 괜찮지만 뷰모델이 정상적으로 관찰(Observe)하는지 확인할 수 있음
////        model.schedule_list?.value?.add(Schedule(summary = "데이터베이스 강의", weather = "SUNNY"))
//
//        /** var i = 1
//        member_schedule_adapter.schedule_list.forEach {
//        notification_builder.setContentTitle(it.weather)
//        notification_builder.setContentText(it.summary)
//        notification_builder.setSmallIcon(Utils.decideWeatherIcon(it.weather))
//        notificationManager.notify(Random.nextInt(), notification_builder.build())
//        i += 1
//        //            if (i==2)   return
//        } **/
//
//    }





    //////////////////////////////////////////////////////////////////////////////////////////////
    //                                           File                                           //
    //////////////////////////////////////////////////////////////////////////////////////////////
    /** Dummy Data 읽기 함수 *********************************************************************/
//    private fun readDummyFile(schedule_list: MutableList<Schedule>) {
//        //파일 입출력을 통한 노티피케이션 테스트 코드
//        val test_file_path = getFilesDir().path + "/testDummy@gmail.com.ics"
//        val file_reader = BufferedReader(FileReader(test_file_path))
//        var line: String = ""
//
//        // 헤더 부분 제거
//        for (i: Int in 1..11) {
//            line = file_reader.readLine() ?: break
//        }
//
//        while (true) {
//            line = file_reader.readLine() ?: break
//            line = line
//
//            val schedule_with_weather = Schedule()
//
//            // .ics 파일 파싱 함수 호출 - UID 부분 처리
//            schedule_with_weather.parsingWithWeather(line)
//
//            // 일정의 구조에서 총 15개의 항목으로 구성
//            // 위에서 UID 부분을 처리했으므로 2번에서 15번까지의 항목 파싱
//            for (i: Int in 2..15) {
//                // EOF에 도달했을 경우에 루프문을 빠져나감
//                line = file_reader.readLine() ?: break
//
//                schedule_with_weather.parsingWithWeather(line)
//            }
//
//            // 제대로된 데이터 형식이 아닌 값은 제외하고 변조 가능한 리스트에 추가
//            if (schedule_with_weather.weather != "") {
//                schedule_list.add(schedule_with_weather)
//            }
//        }
//
//        file_reader.close()
//    }

}
