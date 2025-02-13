package com.kophe.le_sklad_zradn.util.extensions

import java.text.SimpleDateFormat
import java.util.*

const val TIME_FORMAT = "dd.MM.yyyy, HH:mm"
fun Long.timestampToFormattedDate24h(): String =
    SimpleDateFormat(TIME_FORMAT).format(Date(this))

fun Long.timestampToFormattedDate(): String = SimpleDateFormat("dd.MM.YYYY").format(Date(this))
