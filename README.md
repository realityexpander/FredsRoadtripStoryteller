# Fred's Talking Historical Markers

10/8/23
Compose KMP experimental app using Google Maps for iOS and Android

<img width="1700" alt="image" src="https://github.com/realityexpander/GoolgeMapsUsingCocoaPodsExample/assets/5157474/7b0395d3-04e6-48df-9219-990cb10d81e1">

Wiki: [Freds Talking Historical Markers Wiki](https://github.com/realityexpander/FredsHistoryMarkers/wiki)

## Developer notes

Firebase console: https://console.firebase.google.com/u/0/project/talkingmarkers/overview

Source: (Code snapshot taken 10/8/24)
https://github.com/JetBrains/compose-multiplatform/tree/master/examples/cocoapods-ios-example

- Must create AppSecrets.plist from within Xcode - in password vault
- Can get google-services.json from Google Cloud Console
- must do "pod install" from the iosApp folder

### Code style
  - https://github.com/realityexpander/ktor-web-app-server/blob/main/src/main/kotlin/LIBRARY_README.md

### KMP Stuff
- KMP Wizard
  - https://terrakok.github.io/Compose-Multiplatform-Wizard/

- All kinds of libraries
  - https://github.com/terrakok/kmp-awesome
- Settings
  - https://github.com/russhwolf/multiplatform-settings
- Storage (Store5)
 - https://github.com/MobileNativeFoundation/Store

- Location KMM implementation concept
  - https://github.com/line/abc-kmm-location

- kSoup - HTML parser for scraping web pages
  - This thing is a hack and a half way of scraping, but until jSoup is ported, its what we got. 
  -https://github.com/MohamedRejeb/ksoup

- Painter resources
  - https://github.com/JetBrains/compose-multiplatform/blob/master/tutorials/Image_And_Icons_Manipulations/README.md

- Material Design 3
- https://material.io/blog/material-3-compose-1-1
- Drawers
- https://developer.android.com/jetpack/compose/components/drawer

- WebView
- https://github.com/KevinnZou/compose-webview-multiplatform/blob/main/sample/shared/src/commonMain/kotlin/com/kevinnzou/sample/BasicWebViewSample.kt

### Android
- Adding markers
 - https://www.geeksforgeeks.org/how-to-add-multiple-markers-to-google-maps-in-android-using-jetpack-compose/

- Background tracking
  - https://github.com/realityexpander/BackgroundLocationTracking/blob/master/app/src/main/java/com/realityexpander/backgroundlocationtracking/LocationClientImpl.kt

- Location Visualizer
  - https://github.com/JetBrains/compose-multiplatform/blob/7c2bec465489d706451fa3ad5810d060f5bc7773/examples/imageviewer/shared/src/commonMain/kotlin/example/imageviewer/view/LocationVisualizer.common.kt
  - https://github.com/JetBrains/compose-multiplatform/blob/7c2bec465489d706451fa3ad5810d060f5bc7773/examples/imageviewer/shared/src/iosMain/kotlin/example/imageviewer/view/LocationVisualizer.ios.kt

- Problems
- Clustering markers not updated
  - https://github.com/googlemaps/android-maps-compose/issues/269

- Compose Clustering
  - https://betterprogramming.pub/clustering-with-maps-compose-for-android-911a2fe3f08b  

- Compose Samples
  - https://github.com/android/compose-samples  

### iOS
- https://developers.google.com/maps/documentation/ios-sdk/utility/setup
- Location Manager
  - https://developers.google.com/maps/documentation/ios-sdk/current-place-tutorial
- Map Utils (clustering)
- https://github.com/googlemaps/google-maps-ios-utils

- Long running background location updates
  - https://developer.apple.com/forums/thread/69152

- Location picker
  - https://github.com/almassapargali/LocationPicker
  - https://github.com/alessiorubicini/LocationPickerForSwiftUI/blob/master/Sources/LocationPicker/MapView.swift

- Touch map for location
  - https://stackoverflow.com/questions/34431459/ios-swift-how-to-add-pinpoint-to-map-on-touch-and-get-detailed-address-of-th

- Continuous updates
  - https://developer.apple.com/forums/thread/124048

- How to convert iOS UIImage to Compose ImageBitmap in Compose Multiplatform?
  - https://slack-chats.kotlinlang.org/t/12086405/hi-all-how-to-convert-ios-uiimage-to-compose-imagebitmap-in-

### Dev notes
- Why mutableStateList
  - https://stackoverflow.com/questions/67252538/jetpack-compose-update-composable-when-list-changes
- KMM Location services example app
  - https://medium.com/rapido-labs/building-a-kotlin-multiplatform-mobile-sdk-for-location-related-services-488a2855ab23

- Kotlin Swift - Jetpack Compose iOS, ComposeUIViewController, UIViewControllerRepresentable, UIKitView...
  - https://appkickstarter.com/blog/kotlin-swift

### Asset links

- Photoshop editor: https://www.photopea.com/

- Location Markers
- https://pngtree.com/so/location-marker
- https://www.vecteezy.com/png/22187039-map-location-pin-icon
- https://www.google.com/imgres?imgurl=https%3A%2F%2Fpng.pngtree.com%2Fpng-vector%2F20210214%2Fourmid%2Fpngtree-location-marker-png-image_2921053.jpg&tbnid=0jAIDviURQ9mpM&vet=12ahUKEwiMqrbUkZWCAxWFz8kDHe1nDRoQMygGegQIARBU..i&imgrefurl=https%3A%2F%2Fpngtree.com%2Fso%2Flocation-marker&docid=MUeqs9oCST4JyM&w=360&h=360&q=location%20marker%20map%20%2Bfree&ved=2ahUKEwiMqrbUkZWCAxWFz8kDHe1nDRoQMygGegQIARBU
- https://www.google.com/search?q=location+marker+map+%2Bfree&tbm=isch&ved=2ahUKEwj8wsnbkJWCAxUhFN4AHVAFAicQ2-cCegQIABAA&oq=location+marker+map+%2Bfree&gs_lcp=CgNpbWcQAzoECCMQJzoGCAAQCBAeOgQIABAeUNcEWNoUYLoXaABwAHgAgAG9AYgB9wWSAQM0LjOYAQCgAQGqAQtnd3Mtd2l6LWltZ8ABAQ&sclient=img&ei=mhk7ZfyNMaGo-LYP0IqIuAI&bih=1050&biw=1698
- 

# Original Readme:

## [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) iOS CocoaPods example

This example showcases using Kotlin Multiplatform shared module in Swift as a CocoaPods framework.

The official Kotlin documentation provides more information on working with CocoaPods:
* [CocoaPods overview and setup](https://kotlinlang.org/docs/native-cocoapods.html);
* [Add dependencies on a Pod library](https://kotlinlang.org/docs/native-cocoapods-libraries.html);
* [Use a Kotlin Gradle project as a CocoaPods dependency](https://kotlinlang.org/docs/native-cocoapods-xcode.html);


## Setting up your development environment

To setup the environment, please consult these [instructions](https://github.com/JetBrains/compose-multiplatform-template#setting-up-your-development-environment).

## How to run

Choose a run configuration for an appropriate target in IDE and run it.

![run-configurations.png](screenshots/run-configurations.png)
