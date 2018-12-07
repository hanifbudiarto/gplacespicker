import 'dart:async';

import 'package:flutter/services.dart';

class Gplacespicker {
  static const MethodChannel _channel = const MethodChannel('gplacespicker');

  static Future<String> openPlacePicker() async {
    String latLngJson = await _channel.invokeMethod('openPlacePicker');
    return latLngJson;
  }

  static Future openNavigation(double latitude, double longitude) async {
    await _channel.invokeMethod('openNavigation',
        <String, dynamic>{'latitude': latitude, 'longitude': longitude});
  }
}
