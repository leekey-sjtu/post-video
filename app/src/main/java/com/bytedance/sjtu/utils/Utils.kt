package com.bytedance.sjtu.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object Utils {

    fun getRetrofit(baseUrl: String): Retrofit {  // 设置新的baseUrl
        val client = OkHttpClient.Builder()
            .connectTimeout(15000, TimeUnit.MILLISECONDS)  // 预留足够的时间连接服务器
            .readTimeout(15000, TimeUnit.MILLISECONDS)  // 预留足够的时间处理数据
            .build()
        val factory = GsonConverterFactory.create()
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(factory)
            .build()
    }

    @SuppressLint("Range")
    fun uriToPath(mContext: Context, uri: Uri): String? {
        var path: String? = null
        val cursor = mContext.contentResolver.query(uri, null, null, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path
    }

}