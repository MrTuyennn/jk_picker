import 'package:flutter/material.dart';
import 'package:jk_picker/components/jk_text.dart';

class HeaderScreenMain extends StatelessWidget {
  const HeaderScreenMain(
      {super.key,
      required this.text,
      this.childHeader,
      this.onPress,
      this.centerTitle,
      this.iconRight,
      this.borderColor,
      this.isBorderBottm = true,
      this.isBack = true});

  final String? text;
  final Widget? childHeader;
  final Function()? onPress;
  final bool? centerTitle;
  final Widget? iconRight;
  final Color? borderColor;
  final bool? isBorderBottm;
  final bool isBack;

  @override
  Widget build(BuildContext context) {
    return AppBar(
      automaticallyImplyLeading: isBack,
      title: text != null && text != ""
          ? JkText(
              bold: true,
              text: text ?? "",
              fontWeight: FontWeight.w700,
              maxLine: 1,
              overflow: TextOverflow.ellipsis,
            )
          : childHeader,
      actions: [
        Container(
          child: iconRight ?? const SizedBox(),
        ),
      ],
      centerTitle: centerTitle ?? false,
      backgroundColor: Colors.white,
      iconTheme: const IconThemeData(color: Colors.black),
      shape: Border(
        bottom: BorderSide(
            width: isBorderBottm! ? 0.2 : 0,
            color: isBorderBottm! ? borderColor ?? Colors.black : Colors.transparent),
      ),
    );
  }
}
