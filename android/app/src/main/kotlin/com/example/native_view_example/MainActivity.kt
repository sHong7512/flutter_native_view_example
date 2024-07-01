package com.example.native_view_example

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import io.flutter.plugins.GeneratedPluginRegistrant
import com.example.native_view_example.SurfaceCapture

class MainActivity : FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        GeneratedPluginRegistrant.registerWith(flutterEngine)
        flutterEngine
            .platformViewsController
            .registry
            .registerViewFactory(
                "plugin/native_widget",
                NativeViewFactory(flutterEngine.dartExecutor.binaryMessenger)
            )
        SurfaceCapture().makeCustomChannel(flutterEngine.dartExecutor.binaryMessenger, activity)
    }
}

private class NativeViewFactory(val binaryMessenger: BinaryMessenger) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?

        return NativeView(context, viewId, creationParams, binaryMessenger)
    }
}

private class NativeView(
    context: Context?,
    id: Int,
    creationParams: Map<String?, Any?>?,
    binaryMessenger: BinaryMessenger
) : PlatformView {
    private val textView: TextView = TextView(context)
    private val channelName = "native_widget/textView"

    init {
        val createMessage: String = creationParams?.get("key").toString()

        textView.textSize = 30f
        textView.setBackgroundColor(Color.rgb(255, 255, 255))
        textView.text = "Rendered on a native Android view (message: $createMessage)"

        MethodChannel(binaryMessenger, channelName).apply {
            setMethodCallHandler { call, result ->
                when (call.method) {
                    "setText" -> {
                        textView.text = call.argument<String>("text")
                        result.success(true)
                    }
                    else -> result.notImplemented()
                }
            }
        }
    }

    override fun getView(): View = textView

    override fun dispose() {}
}
