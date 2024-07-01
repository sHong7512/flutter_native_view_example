import 'dart:math';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class ExampleAndroidView extends StatelessWidget {
  ExampleAndroidView({super.key});

  static const String PLUGIN_VIEW_TYPE = "plugin/native_widget";
  final Map<String, dynamic> creationParams = <String, dynamic>{};

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Expanded(
          child: AndroidView(
            viewType: PLUGIN_VIEW_TYPE,
            layoutDirection: TextDirection.ltr,
            creationParams: creationParams,
            creationParamsCodec: const StandardMessageCodec(),
          ),
        ),
        ElevatedButton(
          onPressed: () {
            const methodChannel = MethodChannel('native_widget/textView');
            callMethod(methodChannel, 'setText', {'text': '${Random().nextInt(1000)}'});
          },
          child: const Text('Generate Random Number'),
        ),
        ElevatedButton(
          onPressed: () async {
            const methodChannel = MethodChannel('native_widget/capture');
            final str = await callMethod(methodChannel, 'screenshot');
            print('sHong] $str');
          },
          child: const Text('Capture view'),
        ),
      ],
    );
  }

  Future<String> callMethod(MethodChannel channel, String method, [Map<String, String>? args]) async {
    try {
      final result = await channel.invokeMethod(method, args);
      return '$result';
    } on PlatformException catch (e) {
      return 'error message : ${e.message}';
    }
  }
}
