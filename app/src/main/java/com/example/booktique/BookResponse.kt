package com.example.booktique
import android.icu.text.CaseMap.Title
import com.google.firebase.database.IgnoreExtraProperties
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookResponse(
    val items: List<BookItem>
)

data class BookItem(
    val volumeInfo: VolumeDet,

)


data class VolumeDet(
    val imageLinks: ImageLinks = ImageLinks(),
    val title: String?="",
    val authors: List<String> = emptyList(),
    val language: String?="",
    val pageCount: Int?=0,
    val id: String?="",
    val description: String?="",
    val categories: List<String> = emptyList()

)


data class ImageLinks(

    val smallThumbnail: String?=""
)
