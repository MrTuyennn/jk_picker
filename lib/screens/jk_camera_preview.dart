import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:jk_picker/extensions/channel/channel.dart';
import 'package:jk_picker/extensions/components/screen_main.dart';

class JkCameraPreview extends StatefulWidget {
  const JkCameraPreview({super.key});

  @override
  State<JkCameraPreview> createState() => _JkCameraPreviewState();
}

class _JkCameraPreviewState extends State<JkCameraPreview> {
  @override
  Widget build(BuildContext context) {
    return ScreenMain(
        child: Center(
      child: UiKitView(
        viewType: jkCameraID,
        creationParams: const {},
        layoutDirection: TextDirection.ltr,
        creationParamsCodec: const StandardMessageCodec(),
      ),
    ));
  }
}
