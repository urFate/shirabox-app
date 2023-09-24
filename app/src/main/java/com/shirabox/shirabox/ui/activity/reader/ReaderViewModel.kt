package com.shirabox.shirabox.ui.activity.reader

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.shirabox.shirabox.db.AppDatabase
import com.shirabox.shirabox.model.ContentType

class ReaderViewModel(
    context: Context,
    val contentUid: Int,
    val contentName: String,
    val contentAltName: String,
    val contentType: ContentType,
    val startPage: Int,
) : ViewModel() {
    val db = AppDatabase.getAppDataBase(context)

    val pages = mutableStateListOf<String>()
    var controlsVisibilityState by mutableStateOf(true)
    val readingMode by mutableStateOf(ReadingMode.LEFT_TO_RIGHT_HORIZONTAL)

}