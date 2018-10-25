import 'dart:async';

import 'package:flutter/services.dart';

class Gplacespicker {
  static const MethodChannel _channel =
      const MethodChannel('gplacespicker');
      
  static Future<String> openPlacePicker() async {
    String latLngJson = await _channel.invokeMethod('openPlacePicker');  
    return latLngJson;
  }
}
