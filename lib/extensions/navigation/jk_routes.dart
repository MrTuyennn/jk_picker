import 'package:flutter/material.dart';
import 'package:jk_picker/extensions/navigation/path_app.dart';
import 'package:jk_picker/screens/index.dart';

class JkRoutes {
  static Route generateRoutes(RouteSettings setting) {
    switch (setting.name) {
      case PathApp.JKImagePikcer:
        return _buildPageRoute(const JkImagePicker(), setting);
      case PathApp.JKCameraPreview:
        return _buildPageRoute(const JkCameraPreview(), setting);
      default:
        return _buildPageRoute(const JkImagePicker(), setting);
    }
  }

  static MaterialPageRoute _buildPageRoute(Widget page, RouteSettings? settings) {
    return MaterialPageRoute(settings: settings, builder: (context) => page);
  }
}
