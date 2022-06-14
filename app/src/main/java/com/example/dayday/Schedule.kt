package com.example.dayday

import android.util.Log

// 데이터 클래스로 만듬
data class Schedule( var uid: String = "",
                     var created: String = "",
                     var description: String = "",
                     var last_modified: String = "",
                     var location: String = "",
                     var sequence: String = "",
                     var status: String = "",
                     var summary: String = "",
                     var transp: String = "",
                     var end: String = "",
                     var begin: String = "",
                     var dtstart: String = "",
                     var dtend: String = "",
                     var dtstamp: String = "",
                     var weather: String = "") {
//    var uid = ""
//    var created = ""
//    var description = ""
//    var last_modified = ""
//    var location = ""
//    var sequence = ""
//    var status = ""
//    var summary = ""
//    var transp = ""
//    var end = ""
//    var begin = ""
//    var dtstart = ""
//    var dtend = ""
//    var dtstamp = ""
//    var weather = ""

    // .ics 파일에서 읽어온 라인마다 값을 파싱해서 넣어주는 함수
    fun parsingWithWeather(line: String) {
        val before_delimeter = line.substringBefore(":") // .ics 파일 구조 참고
        val after_delimeter = line.substringAfter(":") // .ics 파일 구조 참고

        Log.i("TEST - before delimeter", before_delimeter)
        Log.i("TEST - after delimeter", after_delimeter)

        // 라인마다 읽어나가면서 각 필드에 값을 대입
        if (before_delimeter.equals("UID")) {
            this.uid = after_delimeter
        }
        else if (before_delimeter.equals("CREATED")) {
            this.created = after_delimeter
        }
        else if (before_delimeter.equals("DESCRIPTION")) {
            this.description = after_delimeter
        }
        else if (before_delimeter.equals("LAST-MODIFIED")) {
            this.last_modified = after_delimeter
        }
        else if (before_delimeter.equals("LOCATION")) {
            this.location = after_delimeter
        }
        else if (before_delimeter.equals("SEQUENCE")) {
            this.sequence = after_delimeter
        }
        else if (before_delimeter.equals("STATUS")) {
            this.status = after_delimeter
        }
        else if (before_delimeter.equals("SUMMARY")) {
            this.summary = after_delimeter
        }
        else if (before_delimeter.equals("TRANSP")) {
            this.transp = after_delimeter
        }
        else if (before_delimeter.equals("END")) {
            this.end = after_delimeter
        }
        else if (before_delimeter.equals("BEGIN")) {
            this.begin = after_delimeter
        }
        else if (before_delimeter.equals("DTSTART")) {
            this.dtstart = after_delimeter
        }
        else if (before_delimeter.equals("DTEND")) {
            this.dtend = after_delimeter
        }
        else if (before_delimeter.equals("DTSTAMP")) {
            this.dtstamp = after_delimeter
        }
        else if (before_delimeter.equals("WEATHER")) {
            this.weather = after_delimeter
        }
    }
}