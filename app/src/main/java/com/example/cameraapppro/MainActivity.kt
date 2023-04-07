package com.example.cameraapppro

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.cameraapppro.databinding.ActivityMainBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var filePath: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 인텐트를 통해 갤러리 앱에서 클릭을 하면 클릭된 이미지 Uri를 가져와서, 컨텐트리졸버를 이용해서 inputStream, BitMapFactory를 통해서
        // 이미지 뷰 가져온다.
        val requestLauncher: ActivityResultLauncher<Intent> =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                if (it.resultCode == RESULT_OK) {
                    // 1.1 이미지를 가져오면 OOM이 발생 할 수 있으므로, 화면에 출력될 원하는 사이즈의 비율을 설정해야된다.
                    val inSampleSize = calculateInSampleSize(
                        Uri.fromFile(File(filePath)),
                        resources.getDimensionPixelSize(R.dimen.imgSize),
                        resources.getDimensionPixelSize(R.dimen.imgSize)
                    )
                    // 1.2 비트맵 옵션설정 비율설정
                    val option = BitmapFactory.Options()
                    option.inSampleSize = inSampleSize

                    try {
                        // 1.4 inputStream으로 비트맵팩토리를 이용해서 이미지를 가져온다. (OOM 방지하기 위해서, 옵션에 사이즈 비율을 저장)
                        val bitmap = BitmapFactory.decodeFile(filePath, option)
                        // 1.5 이미지뷰에 비트맵을 저장시키면 된다.
                        bitmap?.let {
                            binding.ivPicture.setImageBitmap(bitmap)
                        } ?: let {
                            Log.e("MainActivity", "bitmapFactory를 통해서 가져온 비트맵 null 발생")
                        }
                    } catch (e: java.lang.Exception) {
                        Log.e("MainActivity", "${e.printStackTrace()}")
                    }
                }
            }

        // 2. 갤러리앱에 암시적 인텐트 방법으로 요청
        binding.btnCallImage.setOnClickListener {
            // 2.1 카메라앱이 저장될 파일명을 만듦
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            // 2.2 카메라앱이 저장될 앱폴더 위치를 가져옴
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            // 2.3 실제 파일명을 만들어서 앱폴더 위치에 저장
            val file = File.createTempFile("__jpg_${timeStamp}_", ".jpg", storageDir)
            filePath = file.absolutePath
            // 2.4 filePath Provider uri 경로를 만듦
            val uri =
                FileProvider.getUriForFile(this, "com.example.cameraapppro.fileprovider", file)

            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
            requestLauncher.launch(intent)
        }
    }

    // 이미지 비율을 계산하는 함수
    fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        // 이미지의 정보만 가져옴
        options.inJustDecodeBounds = true

        try {
            // 컨텐트리졸버를 이용해 이미지 정보를 다시 가져온다.
            var inputStream = contentResolver.openInputStream(uri)
            // 진짜 inputStream 통해서 비트맵을 가져오는것이 아니라, 비트맵 정보만 options 저장해서 가져온다.
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream!!.close()
            inputStream = null
        } catch (e: java.lang.Exception) {
            Log.e("MainActivity", "calculateInSampleSize inputStream ${e.printStackTrace()}")
        }

        // 갤러리앱에서 가져올 실제 이미지 사이즈
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}


