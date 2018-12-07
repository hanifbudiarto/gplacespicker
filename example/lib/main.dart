import 'package:flutter/material.dart';

import 'package:gplacespicker/gplacespicker.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String latLng = "";

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: Column(
          mainAxisSize: MainAxisSize.min,
          children: <Widget>[
            Text(latLng),
            RaisedButton(
              child: Text("pick"),
              onPressed: () async {
                String latLng = await Gplacespicker.openPlacePicker();
                setState(() {
                  this.latLng = latLng;
                });
              },
            ),
            RaisedButton(
              child: Text("navigate"),
              onPressed: () async {
                await Gplacespicker.openNavigation(-7.2809592, 112.7811952);
              },
            )
          ],
        ),
      ),
    );
  }
}
