package com.example.pulsmesserv2

import android.content.Context

class BPMModel  {

    var bpm: Int = 0
    var avgBpm : Int = 0
    var date: String = ""
    var selected: Boolean = false
    constructor(bpm: Int,avgBpm: Int, date: String,selected: Boolean) {
        this.bpm = bpm
        this.avgBpm = avgBpm
        this.date = date
        this.selected = false
    }






}