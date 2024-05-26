package com.example.pulsmesserv2

import android.content.Context

class BPMModel  {

    var bpm: Int = 0
    var date: String = ""
    var selected: Boolean = false
    constructor(bpm: Int, date: String,selected: Boolean) {
        this.bpm = bpm
        this.date = date
        this.selected = false
    }






}