import 'package:flutter/material.dart';
import 'package:jk_picker/components/jk_text.dart';
import 'package:jk_picker/extensions/components/screen_main.dart';

class JkCameraPreview extends StatefulWidget {
  const JkCameraPreview({super.key});

  @override
  State<JkCameraPreview> createState() => _JkCameraPreviewState();
}

class _JkCameraPreviewState extends State<JkCameraPreview> {
  @override
  Widget build(BuildContext context) {
    return const ScreenMain(child: JkText(text: 'text'));
  }
}
