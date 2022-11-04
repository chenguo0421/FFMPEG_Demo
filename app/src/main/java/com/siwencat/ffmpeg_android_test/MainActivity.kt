package com.siwencat.ffmpeg_android_test

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.siwencat.ffmpeg_android_test.adapter.ImageItemAdapter
import com.siwencat.ffmpeg_android_test.util.SecurityUtils
import com.siwencat.ffmpeg_android_test.view.LinearDivideDecoration
import java.io.File
import java.util.concurrent.Executors


class MainActivity : AppCompatActivity() {
    private var adapter: ImageItemAdapter? = null
    private var currentPathMD5: String? = null
    private lateinit var tempPath: String
    private var choseVideo: AppCompatTextView? = null
    private var videoView: VideoView? = null
    private var rvView:RecyclerView? = null
    private var activityLauncher:ActivityResultLauncher<Intent>? = null
    private var mediaCtrl: MediaController? = null

    private var threadPool = Executors.newSingleThreadExecutor()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == Activity.RESULT_OK) {
                val selectedVideo: Uri? = it.data?.data
                selectedVideo?.let { it1 ->
                    val filePathColumn = arrayOf(MediaStore.Video.Media.DATA)

                    val cursor: Cursor? = contentResolver.query(
                        it1,
                        filePathColumn, null, null, null
                    )
                    cursor?.moveToFirst()

                    val columnIndex: Int? = cursor?.getColumnIndex(filePathColumn[0])
                    val path = cursor?.getString(columnIndex ?: 0)
                    cursor?.close()
                    onVideoSelect(path)
                }
            }
        }

        initViews()
        initData()
        initListener()


        threadPool.execute {
            val cmd = "-version"
            executeFFMPeg(cmd)
        }
    }

    private fun initData() {
        tempPath = getExternalFilesDir("ffmpeg")?.absolutePath!!
        val fileDir = File(tempPath)
        if(!fileDir.exists()){
            fileDir.mkdirs()
        }
    }

    private fun executeFFMPeg(cmd: String): Boolean {
        val rc = FFmpeg.execute(cmd)
        var ret = false
        when (rc) {
            Config.RETURN_CODE_SUCCESS -> {
                Log.i(Config.TAG, "命令执行成功")
                ret = true
            }
            Config.RETURN_CODE_CANCEL -> {
                Log.i(Config.TAG, "用户取消了命令")
            }
            else -> {
                Log.i(Config.TAG, String.format("命令执行失败, 返回值=%d", rc))
            }
        }
        Config.printLastCommandOutput(Log.INFO)
        return ret
    }

    private fun initListener() {
        choseVideo?.setOnClickListener {
            choseVideo()
        }
    }

    private fun choseVideo(){
        val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        activityLauncher?.launch(i)
    }


    private fun onVideoSelect(path: String?) {
        path?.let {
            val md5Str = SecurityUtils.getMD5(it)
            currentPathMD5 = md5Str
            videoView?.setVideoPath(path)

            mediaCtrl = MediaController(this)
            videoView?.setMediaController(mediaCtrl)
            mediaCtrl?.setMediaPlayer(videoView)
            videoView?.requestFocus()

            videoView?.start()

            videoView?.setOnCompletionListener { mp ->

                Log.d("onVideoSelect","视频播放完毕")
            }
            threadPool.execute {
                val fileDir = File(tempPath,md5Str)
                if (!fileDir.exists()) {//以前不存在该文件夹，创建文件夹
                    fileDir.mkdirs()
                } else {//以前存在该文件夹，删除文件夹内的全部文件
                    val listFile = fileDir.listFiles()
                    listFile?.let {
                        for (item: File in it) {
                            item.delete()
                        }
                    }
                }

                val cmd = "-i $it -vf fps=1 $fileDir${File.separator}temp_%d.png"
                val ret = executeFFMPeg(cmd)
                if (ret) {
                    onClipFrameSuccess()
                }
            }
        }
    }

    //将图片展示到横向的列表帧列表中
    private fun onClipFrameSuccess() {
        currentPathMD5?.let{
            val fileDir = File(tempPath, it)
            if (fileDir.exists()) {
                val listFile = fileDir.listFiles()
                listFile?.let { it1 ->
                    val list = mutableListOf<String>()
                    for (item: File in it1) {
                        list.add(item.absolutePath)
                    }
                    updateClipImageList(list)
                }
            }
        }
    }

    private fun updateClipImageList(list: MutableList<String>) {
        runOnUiThread {
            if (adapter == null) {
                adapter = ImageItemAdapter(this, list)
                rvView?.adapter = adapter
            } else {
                adapter?.updateClipImageList(list)
            }
        }
    }


    private fun initViews() {
        choseVideo = findViewById(R.id.chose_video)
        videoView = findViewById(R.id.video_view)


        rvView = findViewById(R.id.rv_view)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvView?.layoutManager = layoutManager
        rvView?.addItemDecoration(LinearDivideDecoration(
            context = this,
            orientation = LinearDivideDecoration.HORIZONTAL,
            drawableRes = R.drawable.common_rv_divider_gray,
            bottom = 1f
        ))
    }
}