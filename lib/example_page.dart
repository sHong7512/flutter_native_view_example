import 'package:flutter/material.dart';
import 'package:native_view_example/example_android_view.dart';
import 'example_native_view.dart';

class ExamplePage extends StatelessWidget {
  ExamplePage({Key? key}) : super(key: key);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Container(
          width: MediaQuery.of(context).size.width,
          height: MediaQuery.of(context).size.height,
          color: Colors.grey,
          // child: ExampleNativeView(),
          child: ExampleAndroidView(),
        ),
      ),
    );
  }
}
