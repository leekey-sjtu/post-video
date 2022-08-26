package com.bytedance.sjtu

import android.R.attr
import android.annotation.SuppressLint
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sjtu.bean.PostVideoBean
import com.bytedance.sjtu.service.VideoService
import com.bytedance.sjtu.utils.Utils.getRetrofit
import com.bytedance.sjtu.utils.Utils.uriToPath
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class MainActivity : AppCompatActivity() {

    private val button : Button by lazy { findViewById(R.id.button) }
    private val tvVideoUri : TextView by lazy { findViewById(R.id.tv_video_uri) }
    private val tvVideoPath : TextView by lazy { findViewById(R.id.tv_video_path) }
    private val tvCoverPath : TextView by lazy { findViewById(R.id.tv_cover_path) }
    private lateinit var videoUri : Uri
    private lateinit var videoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        button.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, 666) //TODO
        }

    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 666) {
            if (data != null) {
                videoUri = data.data!!
                Log.d("wdw", "video uri = $videoUri")
                val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
                val cursor = contentResolver.query(videoUri, filePathColumn, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        videoPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
                    }
                    cursor.close()
                }
                Log.d("wdw", "video path = $videoPath")

                tvVideoUri.text = videoUri.toString()
                tvVideoPath.text = videoPath
                tvCoverPath.text//TODO

//                postVideo(
//                    "wudewei",
//                    "",
//                    "", // 视频封面
//                    videoPath
//                )
            }
        }
    }

    //提交封面和视频
    private fun postVideo(userName: String, extraValue: String, imgPath: String, videoPath: String) {
        Log.e("wdw", "开始提交封面和视频")
        val imageFile = File(imgPath)
        val imageBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("cover_image", imageFile.name, imageBody)
        val videoFile = File(videoPath)
        val videoBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, videoBody)
        getRetrofit("https://bd-open-lesson.bytedance.com/api/invoke/")
            .create(VideoService::class.java)
            .postVideo("121110910068_post", userName, extraValue, imagePart, videoPart)
            .enqueue(object : Callback<PostVideoBean> {
                override fun onResponse(call: Call<PostVideoBean>, response: Response<PostVideoBean>) {
                    if (response.body()?.success == true) {  //若response返回success
                        Log.e("wdw", "视频发布成功")
                    }
                }
                override fun onFailure(call: Call<PostVideoBean>, t: Throwable) {
                    Log.d("wdw", "postVideo failed, $t")
                }
            })
    }

}