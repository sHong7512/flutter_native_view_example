import 'dart:io';
import 'dart:math';

import 'package:flutter/foundation.dart';
import 'package:flutter/gestures.dart';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:flutter/services.dart';

class ExampleNativeView extends StatelessWidget {
  static const String PLUGIN_VIEW_TYPE = "plugin/native_widget";

  const ExampleNativeView({Key? key}) : super(key: key);

  final methodChannel = const MethodChannel('native_widget/textView');

  Future<String> callMethod(String method, [Map<String, String>? args]) async {
    try {
      final result = await methodChannel.invokeMethod(method, args);
      return '$result';
    } on PlatformException catch (e) {
      return 'error message : ${e.message}';
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(child: nativeView()),
        ElevatedButton(
          onPressed: () {
            callMethod('setText', {'text': '${Random().nextInt(1000)}'});
          },
          child: const Text('Generate Random Number'),
        ),
      ],
    );
  }

  Widget nativeView() {
    final Map<String, dynamic> creationParams = <String, dynamic>{'key': 'message'};
    if (Platform.isAndroid) {
      return PlatformViewLink(
        viewType: PLUGIN_VIEW_TYPE,
        surfaceFactory: (context, controller) {
          return AndroidViewSurface(
            controller: controller as AndroidViewController,
            gestureRecognizers: const <Factory<OneSequenceGestureRecognizer>>{},
            hitTestBehavior: PlatformViewHitTestBehavior.opaque,
          );
        },
        onCreatePlatformView: (params) {
          return PlatformViewsService.initSurfaceAndroidView(
            id: params.id,
            viewType: PLUGIN_VIEW_TYPE,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
            onFocus: () {
              params.onFocusChanged(true);
            },
          )
            ..addOnPlatformViewCreatedListener(params.onPlatformViewCreated)
            ..create();
        },
      );
    } else {
      return UiKitView(
        viewType: PLUGIN_VIEW_TYPE,
        layoutDirection: TextDirection.ltr,
        creationParams: creationParams,
        creationParamsCodec: const StandardMessageCodec(),
      );
    }
  }
}
