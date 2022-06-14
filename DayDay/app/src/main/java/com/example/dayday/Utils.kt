package com.example.dayday

class Utils {
    companion object {
        // 날씨와 그에 걸맞는 아이콘을 결정해서 돌려주는 함수
        fun decideWeatherIcon(weather: String): Int {
            if (weather == "0") {
                return R.drawable.sunny
            } else if (weather == "1") {
                return R.drawable.rain
            } else if (weather == "2") {
                return R.drawable.rain_snow
            } else if (weather == "3") {
                return R.drawable.snow
                // 2022-06-02 날씨 조건이 어디에도 해당되지 않을 때.
            } else {
                return R.drawable.sunny
            }
            // 2022-06-02 반드시 날씨를 반환해야 하므로 주석 처리
//            return 0
        }
    }
}
