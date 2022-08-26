package com.bytedance.sjtu

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bytedance.sjtu.bean.PostVideoBean
import com.bytedance.sjtu.service.VideoService
import com.bytedance.sjtu.utils.Utils.getRetrofit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream


class MainActivity : AppCompatActivity() {

    private val btnOpenAlbum : Button by lazy { findViewById(R.id.btn_open_album) }
    private val btnPostVideo : Button by lazy { findViewById(R.id.btn_post_video) }
    private val tvVideoPath : TextView by lazy { findViewById(R.id.tv_video_path) }
    private val tvCoverPath : TextView by lazy { findViewById(R.id.tv_cover_path) }
    private val etUserName : EditText by lazy { findViewById(R.id.et_user_name) }
    private val etExtraValue : EditText by lazy { findViewById(R.id.et_extra_value) }
    private lateinit var videoUri : Uri
    private lateinit var videoPath : String
    private lateinit var coverPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnOpenAlbum.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, VIDEO_ALBUM_REQUEST_CODE)
        }

        btnPostVideo.setOnClickListener {
            if (etUserName.text == null) {
                Toast.makeText(this, "user_name 不能为空！", Toast.LENGTH_SHORT).show()
            } else if (etExtraValue.text == null) {
                Toast.makeText(this, "extra_value 不能为空！", Toast.LENGTH_SHORT).show()
            } else {
                postVideo(
                    "121110910068_portrait",
                    etUserName.text.toString(),
                    etExtraValue.text.toString(),
                    coverPath,
                    videoPath
                )
            }
        }
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        etUserName.text = null
        etExtraValue.text = null
        if (requestCode == VIDEO_ALBUM_REQUEST_CODE) {
            if (data != null) {
                // 获取视频路径
                videoUri = data.data!!
                val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)
                val cursor = contentResolver.query(videoUri, filePathColumn, null, null, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        videoPath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]))
                    }
                    cursor.close()
                }
                // 设置封面路径
                val videoName = File(videoPath).name
                val coverName = videoName.substring(0..videoName.lastIndexOf('.')) + "jpg"
                coverPath = videoPath.substring(0..videoPath.lastIndexOf('/')) + coverName
                // 提取封面
                val bos = BufferedOutputStream(FileOutputStream(coverPath))
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(videoPath)
                val bitmap = retriever.getFrameAtTime(0, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                bos.flush()
                bos.close()
                bitmap?.recycle()

                tvVideoPath.text = videoPath
                tvCoverPath.text = coverPath
            }
        }
    }

    //提交封面和视频
    private fun postVideo(studentId: String, userName: String, extraValue: String, coverPath: String, videoPath: String) {
        Log.e("wdw", "开始提交封面和视频")
        Toast.makeText(this, "开始提交封面和视频", Toast.LENGTH_SHORT).show()
        val coverFile = File(coverPath)
        val coverBody = coverFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val coverPart = MultipartBody.Part.createFormData("cover_image", coverFile.name, coverBody)
        val videoFile = File(videoPath)
        val videoBody = videoFile.asRequestBody("video/mp4".toMediaTypeOrNull())
        val videoPart = MultipartBody.Part.createFormData("video", videoFile.name, videoBody)
        getRetrofit("https://bd-open-lesson.bytedance.com/api/invoke/")
            .create(VideoService::class.java)
            .postVideo(studentId, userName, extraValue, coverPart, videoPart)
            .enqueue(object : Callback<PostVideoBean> {
                override fun onResponse(call: Call<PostVideoBean>, response: Response<PostVideoBean>) {
                    if (response.body()?.success == true) {
                        Log.e("wdw", "视频发布成功")
                        Toast.makeText(this@MainActivity, "视频发布成功", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<PostVideoBean>, t: Throwable) {
                    Log.d("wdw", "postVideo failed, $t")
                }
            })
    }

    companion object {
        const val VIDEO_ALBUM_REQUEST_CODE = 1001
    }

}