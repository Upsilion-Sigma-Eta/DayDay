package com.example.dayday

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherScheduleAdapter (var items : Array<ModelWeather>) : RecyclerView.Adapter<WeatherScheduleAdapter.ViewHolder>() {
    // 뷰 홀더 만들어서 반환, 뷰의 레이아웃은 today_weather_recycleview.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherScheduleAdapter.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.today_weather_recycleview, parent, false)
        return ViewHolder(itemView)
    }

    // 전달받은 위치의 아이템 연결
    override fun onBindViewHolder(holder: WeatherScheduleAdapter.ViewHolder, position: Int) {
        val item = items[position]

        holder.setItem(item)
    }

    // 아이템 개수 리턴
    override fun getItemCount() = items.count()

    // 뷰 홀더 설정
    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        fun setItem(item : ModelWeather) {
            val imgWeather = itemView.findViewById<ImageView>(R.id.weather_icon)       // 날씨 이미지
            val tvTemp = itemView.findViewById<TextView>(R.id.time_temperature)        // 온도
            val tvTime = itemView.findViewById<TextView>(R.id.weather_time)            // 시각
            var timedata = item.Time.substring(0 until 2)

            imgWeather.setImageResource(getWeatherImage(item.sky))
            tvTemp.text = item.temp + "°"
            tvTime.text = timedata + "시"
        }
    }

    fun getWeatherImage(sky : String) : Int {
        // 하늘 상태
        return when(sky) {
            "1" ->R.drawable.sunny                  // 맑음
            "3" ->  R.drawable.cloud                  // 구름 많음
            "4" -> R.drawable.little_cloud            // 흐림
            else -> R.drawable.ic_launcher_foreground // 오류
        }
    }
}