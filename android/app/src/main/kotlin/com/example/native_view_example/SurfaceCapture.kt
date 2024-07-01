package com.example.native_view_example

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.PixelCopy
import android.view.SurfaceView
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import io.flutter.plugin.common.BinaryMessenger
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import io.flutter.plugin.common.MethodChannel

/*
    AndroidViewSurface flutter 위젯 네이티브 캡처용 클래스
    TODO:: 아직 적용중임 (현재 캡처 안됨)
 */
class SurfaceCapture {
    private val customChannel = "native_widget/capture"
    private val methodScreenshot = "screenshot"
    private val dirPath = "DCIM/example"
    private val saveQuality = 100
    private val compressFormat = Bitmap.CompressFormat.JPEG

    private fun fileName() = "example_${System.currentTimeMillis()}"

    fun makeCustomChannel(binaryMessenger: BinaryMessenger, activity: Activity) {
        val methodChannel = MethodChannel(binaryMessenger, customChannel)
        methodChannel.setMethodCallHandler { call, result ->
            when (call.method) {
                methodScreenshot -> {
                    val rootView = activity.window.decorView.rootView as ViewGroup
                    if (rootView.childCount <= 0) {
                        result.success(false)
                    } else {
                        takeScreenshot(activity, rootView.getChildAt(0), result)
                    }
                }

                else -> result.notImplemented()
            }
        }
    }

    // 캡처 요청
    private fun takeScreenshot(activity: Activity, view: View, result: MethodChannel.Result) {

        when (view) {
            is SurfaceView -> {
                captureSurfaceView(activity, view, result)
            }

            is TextureView -> {
                captureTextureView(activity, view, result)
            }

            else -> {
                result.success(false)
            }
        }

    }

    // SurfaceView 캡처
    private fun captureSurfaceView(
        activity: Activity,
        surfaceView: SurfaceView,
        result: MethodChannel.Result
    ) {
        val bitmap =
            Bitmap.createBitmap(surfaceView.width, surfaceView.height, Bitmap.Config.ARGB_8888)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PixelCopy.request(surfaceView, bitmap, { copyResult: Int ->
                if (copyResult == PixelCopy.SUCCESS) {
                    val isSaved = imageSaveJPEG(context = activity, bitmap = bitmap)
                    result.success(isSaved)
                } else {
                    result.success(false)
                }
            }, Handler(Looper.getMainLooper()))
        } else {
            val canvas = Canvas(bitmap)
            surfaceView.draw(canvas)
            canvas.setBitmap(null)
            val isSaved = imageSaveJPEG(context = activity, bitmap = bitmap)
            result.success(isSaved)
        }
    }

    // TextureView 캡처
    private fun captureTextureView(
        activity: Activity,
        textureView: TextureView,
        result: MethodChannel.Result
    ) {
        val bitmap = textureView.bitmap
        if (bitmap != null) {
            val isSaved = imageSaveJPEG(context = activity, bitmap = bitmap)
            result.success(isSaved)
        } else {
            result.success(false)
        }
    }

    // 이미지 저장 요청
    private fun imageSaveJPEG(context: Context, bitmap: Bitmap): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveJPEG(context, bitmap)
        else saveUnderAndroidQJPEG(context, bitmap)

    // SDK 29이상 저장시
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveJPEG(context: Context, bitmap: Bitmap): Boolean {
        val contentValues = ContentValues()
        contentValues.apply {
            put(MediaStore.Images.Media.RELATIVE_PATH, dirPath)
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName())
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        )

        try {
            if (uri != null) {
                val image = context.contentResolver.openFileDescriptor(uri, "w", null)

                if (image != null) {
                    val fos = FileOutputStream(image.fileDescriptor)
                    bitmap.compress(compressFormat, saveQuality, fos)
                    fos.close()

                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, contentValues, null, null)
                    return true
                }
            }
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    // SDK 29미만 저장시
    private fun saveUnderAndroidQJPEG(context: Context, bitmap: Bitmap): Boolean {
        val externalStorage = Environment.getExternalStorageDirectory().absolutePath
        val path = "$externalStorage/$dirPath"
        val dir = File(path)
        if (dir.exists().not()) dir.mkdirs()

        try {
            val fileItem = File("$dir/${fileName()}")
            fileItem.createNewFile()
            val fos = FileOutputStream(fileItem) // 파일 아웃풋 스트림
            bitmap.compress(compressFormat, saveQuality, fos)
            fos.close()

            context.sendBroadcast(
                Intent(
                    Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(fileItem)
                )
            )
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}