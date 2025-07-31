import 'dart:io';

import 'package:flutter/material.dart';
import 'package:jk_picker/extensions/components/header_screen_main.dart';

class ScreenMain extends StatelessWidget {
  const ScreenMain(
      {super.key,
      required this.child,
      this.onBackHandler,
      this.text,
      this.childHeader,
      this.centerTitle,
      this.onPress,
      this.bold,
      this.iconRight,
      this.isBorderBottm = true,
      this.resizeToAvoidBottomInset,
      this.floating,
      this.isBack = true});

  final Widget child;
  final String? text;
  final bool? bold;
  final bool? centerTitle;
  final Function()? onPress;
  final Widget? childHeader;
  final Function()? onBackHandler;
  final Widget? iconRight;
  final bool? isBorderBottm;
  final Widget? floating;
  final bool? resizeToAvoidBottomInset;
  final bool isBack;

  @override
  Widget build(BuildContext context) {
    // ignore: deprecated_member_use
    return SafeArea(
      bottom: true,
      top: false,
      // ignore: deprecated_member_use
      child: WillPopScope(
        onWillPop: () async {
          if (onBackHandler != null) {
            onBackHandler!();
            return Future.value(false);
          } else {
            return Future.value(true);
          }
        },
        child: Scaffold(
          resizeToAvoidBottomInset: resizeToAvoidBottomInset,
          appBar: PreferredSize(
              preferredSize: const Size.fromHeight(50),
              child: HeaderScreenMain(
                text: text,
                childHeader: childHeader,
                onPress: onPress,
                centerTitle: centerTitle,
                iconRight: iconRight,
                isBorderBottm: isBorderBottm,
                isBack: isBack,
              )),
          backgroundColor: Colors.white,
          floatingActionButton: floating,
          body: Container(
            padding: EdgeInsets.only(bottom: Platform.isIOS ? 16 : 0),
            child: child,
          ),
        ),
      ),
    );
  }
}
