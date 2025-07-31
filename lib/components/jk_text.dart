import 'package:flutter/material.dart';

class JkText extends StatelessWidget {
  final String? text;
  final bool? bold;
  final Color? color;
  final double? size;
  final FontWeight? fontWeight;
  final FontStyle? fontStyle;
  final TextAlign? textAlign;
  final String? fontFamily;
  final TextOverflow? overflow;
  final int? maxLine;
  final TextStyle? textStyle;
  final TextDecoration? decoration;
  const JkText(
      {super.key,
      required this.text,
      this.bold,
      this.color,
      this.size,
      this.fontWeight,
      this.textAlign,
      this.fontStyle,
      this.fontFamily,
      this.maxLine,
      this.overflow,
      this.textStyle,
      this.decoration});

  @override
  Widget build(BuildContext context) {
    final defaultTextStyle = TextStyle(
      decoration: decoration ?? TextDecoration.none,
      color: Colors.black,
      fontStyle: fontStyle ?? FontStyle.normal,
      fontWeight: bold == true ? FontWeight.bold : fontWeight ?? FontWeight.w300,
    );
    final sanitizedTextStyle = textStyle?.copyWith(decoration: null);
    final mergedTextStyle = defaultTextStyle.merge(sanitizedTextStyle);
    return Text(
      textScaler: TextScaler.noScaling,
      text == null || text.toString() == "" ? "" : text.toString(),
      maxLines: maxLine,
      overflow: overflow,
      textAlign: textAlign,
      style: mergedTextStyle,
    );
  }
}
