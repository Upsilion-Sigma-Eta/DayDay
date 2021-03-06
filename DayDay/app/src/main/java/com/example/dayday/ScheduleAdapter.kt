package com.example.dayday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.graphics.toColor
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*


class ScheduleAdapter(): RecyclerView.Adapter<ScheduleAdapter.ViewHolder>() {

    // 2022-05-28 뷰모델에서 사용해야 하므로 companion object로 변경
    companion object {
        var schedule_list = mutableListOf<Schedule>()
        val weatherArr = MainActivity.getInstance()!!.weatherArr
    }
//    //2022.06.02 : 사용 안함
//    inner class TodayListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        //텍스트뷰에 데이터 세팅
//        fun setItem(item : ModelWeather) {
//            //val tvTime = itemView.findViewById<TextView>(R.id.tvTime)           // 시각
//            //val tvRainType = itemView.findViewById<TextView>(R.id.tvRainType)   // 강수 형태
//            //val tvHumidity = itemView.findViewById<TextView>(R.id.tvHumidity)   // 습도
//            //val tvSky = itemView.findViewById<TextView>(R.id.tvSky)             // 하늘 상태
//            val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)           // 온도
//
//            //tvTime.text = item.fcstTime
//            //tvRainType.text = getRainType(item.rainType)
//            //tvHumidity.text = item.humidity
//            //tvSky.text = getSky(item.sky)
//            tvTemp.text = item.temp + "°"
//
//            // 2022-05-15 추가 사항 : 색 지정을 위한 온도 검사
//            if (item.temp != "") {
//                val temp_int = item.temp.toInt()
//                val constairnt_layout = itemView.findViewById<ConstraintLayout>(R.id.constraint_layout)
//                if (temp_int <= 15) {
//                    constairnt_layout.setBackgroundColor(0x4D00299C)
//                } else if (temp_int <= 12) {
//                    constairnt_layout.setBackgroundColor(0x4D6182D6)
//                } else if (temp_int <= 33) {
//                    constairnt_layout.setBackgroundColor(0x4DBFBFBF)
//                } else if (temp_int <= 35) {
//                    constairnt_layout.setBackgroundColor(0x4DE86F62)
//                } else if (temp_int >= 35) {
//                    constairnt_layout.setBackgroundColor(0x4DA41000)
//                }
//            }
//
//            // 2022-05-15 추가 사항 : 색깔에 아이콘이 가려지지 않도록 조치
//            val image_view = itemView.findViewById<ImageView>(R.id.weather_icon_image_view)
//            image_view.bringToFront()
//        }
//    }

    inner class ViewHolder(parent: ViewGroup, itemView: View) : RecyclerView.ViewHolder(itemView) {

        val summary: TextView = itemView.findViewById(R.id.schedule_summary_text_view)
        val calendar_date: TextView = itemView.findViewById(R.id.schedule_date_text_view)
        //val description: TextView = itemView.findViewById(R.id.schedule_description_text_view)
        val weather_icon: ImageView = itemView.findViewById(R.id.weather_icon_image_view)
        val parentView = parent

        //텍스트뷰에 데이터 세팅
        fun setItem(item : Schedule) {
            //val tvTime = itemView.findViewById<TextView>(R.id.tvTime)           // 시각
            //val tvRainType = itemView.findViewById<TextView>(R.id.tvRainType)   // 강수 형태
            //val tvHumidity = itemView.findViewById<TextView>(R.id.tvHumidity)   // 습도
            //val tvSky = itemView.findViewById<TextView>(R.id.tvSky)             // 하늘 상태
            val tvTemp = itemView.findViewById<TextView>(R.id.tvTemp)           // 온도
            val constraintBar_layout = itemView.findViewById<LinearLayout>(R.id.constraintBar_layout)

            //tvTime.text = item.fcstTime
            //tvRainType.text = getRainType(item.rainType)
            //tvHumidity.text = item.humidity
            //tvSky.text = getSky(item.sky)

            var raw_day_txt : List<String>

            if (item.begin.contains("T") == false) {
                raw_day_txt = item.dtstart.split("T")
            }
            else {
                raw_day_txt = item.begin.split("T")
            }

            var day_txt = raw_day_txt[0].split("-")
            calendar_date.text = day_txt[1] + "월 " +day_txt[2] + "일"

            // 2022-06-09 하루종일인 일정이 있을 때 크래시 방지
            var calendar_day = ""
            var time_txt : List<String>

            if (raw_day_txt.count() < 2) {
                time_txt = emptyList()
            }
            else {
                time_txt = raw_day_txt[1].split(":")
            }

            if (time_txt.isEmpty() == true) {
                calendar_day = day_txt[0]
            }
            else {
                calendar_day = day_txt[2] + time_txt[0] + time_txt[1]
                //ddTTMM
            }

            var day = SimpleDateFormat("dd", Locale.getDefault()).format(Calendar.getInstance().time)
            val cal = Calendar.getInstance()
            var time = SimpleDateFormat("HH", Locale.getDefault()).format(cal.time) // 현재 시간
            var weatherday = (day.toInt()+((time.toInt()+71)/24)).toString()
            time = ((time.toInt()+71)%24).toString() + "00"

            if(calendar_day < weatherday+time){
                    setWeather(36,128, item)

                    val sky = (
                            when(item.weather.rainType) {
                                "0" -> item.weather.sky ?: "0"
                                else -> (1+10).toString()
                            }
                    )
                    weather_icon.setImageResource(Utils.decideWeatherIcon(sky))
            }
            else
                constraintBar_layout.visibility = View.INVISIBLE

            // 2022-05-15 추가 사항 : 색 지정을 위한 온도 검사
            // 2022-06-02 : 컬러코드 사용
//            if (item.temp != "") {
//                val temp_int = item.temp.toInt()
//                val constairnt_layout = itemView.findViewById<ConstraintLayout>(R.id.constraint_layout)
//                if (temp_int <= 15) {
//                    constairnt_layout.setBackgroundColor(0x4D00299C)
//                } else if (temp_int <= 12) {
//                    constairnt_layout.setBackgroundColor(0x4D6182D6)
//                } else if (temp_int <= 33) {
//                    constairnt_layout.setBackgroundColor(0x4DBFBFBF)
//                } else if (temp_int <= 35) {
//                    constairnt_layout.setBackgroundColor(0x4DE86F62)
//                } else if (temp_int > 35) {
//                    constairnt_layout.setBackgroundColor(0x4DA41000)
//                }
//            }
            if (item.weather.temp != "") {
                val temp_int = item.weather.temp.toInt()
                //val constairnt_layout = itemView.findViewById<ConstraintLayout>(R.id.constraint_layout)
                val constraintBar_layout = itemView.findViewById<View>(R.id.constraintBar_layout)
                if (temp_int <= 15) {
                    //constairnt_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Clod_level2_background))
                    constraintBar_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Clod_level2))
                } else if (temp_int <= 12) {
                    //constairnt_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Clod_level1_background))
                    constraintBar_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Clod_level1))
                } else if (temp_int <= 33) {
                    //constairnt_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.MainColor_background))
                    constraintBar_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.MainColor))
                } else if (temp_int <= 35) {
                    //constairnt_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Hot_level1_background))
                    constraintBar_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Hot_level1))
                } else if (temp_int >= 35) {
                    //constairnt_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Hot_level2_background))
                    constraintBar_layout.setBackgroundColor(parentView.context.getResources().getColor(R.color.Hot_level2))
                }
            }

            // 2022-05-15 추가 사항 : 색깔에 아이콘이 가려지지 않도록 조치
            //val image_view = itemView.findViewById<ImageView>(R.id.weather_icon_image_view)
            //image_view.bringToFront()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.schedule_recycleview, parent, false)

        return ViewHolder(parent, view)
    }

    override fun onBindViewHolder(holder: ScheduleAdapter.ViewHolder, position: Int) {
        holder.summary.text = schedule_list[position].summary
        //holder.description.text = schedule_list[position].description
        // 2022-06-09 크래시 방지를 위한 검사 절차 추가

        holder.setItem(schedule_list[position])
    }

    override fun getItemCount(): Int {
        return schedule_list.count()
    }


    // 강수 형태
    fun getRainType(rainType : String) : String {
        return when(rainType) {
            "0" -> "없음"
            "1" -> "비"
            "2" -> "비/눈"
            "3" -> "눈"
            "4" -> "소나기"
            "5" -> "빗방울"
            "6" -> "빗방울/눈날림"
            "7" -> "눈날림"
            else -> "오류"
        }
    }

    // 하늘 상태
    fun getSky(sky : String) : String {
        return when(sky) {
            "1" -> "맑음"
            "3" -> "구름 많음"
            "4" -> "흐림"
            else -> "오류 rainType : " + sky
        }
    }

    fun setWeather(nx : Int, ny : Int, item:Schedule) {

//        var raw_date_txt : List<String>
//        if (!schedule_list.begin.contains("T")) {
//            raw_date_txt = schedule_list.dtstart.split("T")
//        }
//        else {
//            raw_date_txt = schedule_list.begin.split("T")
//        }
//
//        val date_txt = raw_date_txt[0].split("-")
//        var basedate = date_txt[0] + date_txt[1] + date_txt[2]
//
//        val time_txt = raw_date_txt[1].split(":")
//        val ctime = time_txt[0]
//
//
//        var d = basedate.toInt()-base_date.toInt()
//        var t = base_time.toInt()-ctime.toInt()
//        var cnt = d*24+t


        val weathered = MainActivity.getInstance()?.weatheritem
        // 비동기적으로 실행하기
        if (weathered != null) {
            if(weathered.isNotEmpty()){
                for (i in 0..11) {

                    //if(index==cnt) {
                    when (weathered[i].category) {
                        "PTY" -> item.weather.rainType = weathered[i].fcstValue     // 강수 형태
                        "REH" -> item.weather.humidity = weathered[i].fcstValue     // 습도
                        "SKY" -> item.weather.sky = weathered[i].fcstValue          // 하늘 상태
                        "TMP" -> item.weather.temp = weathered[i].fcstValue         // 기온
                        else -> continue
                    }

                    item.weather.Time = weathered[i].fcstTime
                    //}
                    //else if(index>cnt)
                    //    return
                }
            }
        }

    }

    /** 예보 시간 반환 함수 ************************************************************************/
    fun getTime(time : String) : String {
        var result = ""
        when(time) {
            in "00".."02" -> result = "2300"    // 00~02
            in "03".."05" -> result = "0200"    // 03~05
            in "06".."08" -> result = "0500"    // 06~08
            in "09".."11" -> result = "0800"    // 09~11
            in "12".."14" -> result = "1100"    // 12~14
            in "15".."17" -> result = "1400"    // 15~17
            in "18".."20" -> result = "1700"    // 18~20
            else -> result = "2000"             // 21~23
        }
        return result
    }
}



