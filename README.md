# gplacespicker

A plugin for PlacePicker Android
(iOS not available yet)

## Getting Started

Add permission to AndroidManifest.xml

```
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
```

Add meta-data inside <application> tag
```
<meta-data
android:name="com.google.android.geo.API_KEY"
android:value="YOUR_API_KEY"/>
```

## How to use
```
String result = await Gplacespicker.openPlacePicker();
```

Json string result, you have to parse yourself.
```
{"latitude":44443,"longitude":-78676,"place":"Somewhere"}
```