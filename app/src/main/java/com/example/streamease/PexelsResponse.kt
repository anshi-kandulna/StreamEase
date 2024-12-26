package com.example.streamease

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VideoResponse(
    val page: Int,
    val per_page: Int,
    val total_results: Int,
    val url: String,
    val videos: List<Video>
) : Parcelable

@Parcelize
data class Video(
    val id: Int,
    val width: Int,
    val height: Int,
    val url: String,
    val image: String,
    val duration: Int,
    val user: User,
    val video_files: List<VideoFile>,
    val video_pictures: List<VideoPicture>
) : Parcelable

@Parcelize
data class User(
    val id: Int,
    val name: String,
    val url: String
) : Parcelable

@Parcelize
data class VideoFile(
    val id: Int,
    val quality: String,
    val file_type: String,
    val width: Int?,
    val height: Int?,
    val link: String
) : Parcelable

@Parcelize
data class VideoPicture(
    val id: Int,
    val picture: String,
    val nr: Int
) : Parcelable