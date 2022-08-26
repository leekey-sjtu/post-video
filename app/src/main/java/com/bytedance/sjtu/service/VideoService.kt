package com.bytedance.sjtu.service

import com.bytedance.sjtu.bean.PostVideoBean
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.*

interface VideoService {

    @Multipart
    @POST("video")
    fun postVideo(
        @Query("student_id") student_id: String,
        @Query("user_name") user_name: String,
        @Query("extra_value") extra_value: String,
        @Part cover_image: MultipartBody.Part,
        @Part video: MultipartBody.Part
    ): Call<PostVideoBean>

}