import 'package:flutter/material.dart';
import 'package:jk_picker/extensions/navigation/jk_routes.dart';
import 'package:jk_picker/screens/jk_image_picker.dart';

void main() {
  runApp(const MaterialApp(
    onGenerateRoute: JkRoutes.generateRoutes,
    home: JkImagePicker(),
  ));
}
