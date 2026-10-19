package com.mangareader.app.ui.components

import java.text.DateFormat
import java.util.Date

fun formatDate(millis: Long): String =
    DateFormat.getDateInstance(DateFormat.SHORT).format(Date(millis))
