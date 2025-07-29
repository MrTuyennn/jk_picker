import 'package:flutter/material.dart';
import 'package:jk_picker/extensions/channel/channel.dart';

class JkImagePicker extends StatefulWidget {
  const JkImagePicker({super.key});

  @override
  State<JkImagePicker> createState() => _JkImagePickerState();
}

class _JkImagePickerState extends State<JkImagePicker> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: GestureDetector(
          onTap: () {
            methodChannelJKPicker.invokeMethod(methodGetAlbums);
          },
          child: const Text("Get All Album"),
        ),
      ),
    );
  }
}
