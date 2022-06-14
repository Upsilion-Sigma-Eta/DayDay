package com.example.dayday

import android.text.Editable
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

// 동적으로 추가 및 삭제되는 데이터를 반영하기 위한 뷰모델
class ScheduleViewModel : ViewModel() {
    val schedule_list = MutableLiveData<MutableList<Schedule>>()
    var weather_data = MutableLiveData<Array<ModelWeather>>()
    // 2022-05-28 더이상 사용하지 않음. 
//    private val schedule_list_inner = mutableListOf<Schedule>()
//    private val weather_data_inner = mutableListOf<ModelWeather>()

    init {
        // 2022-05-28 변경됨. 어댑터의 멤버들을 뷰모델에서 사용함으로써 데이터 초기화 방지
        schedule_list.value = ScheduleAdapter.schedule_list
        weather_data.value = ScheduleAdapter.weatherArr
    }
}
