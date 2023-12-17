![image](https://github.com/realityexpander/FredsRoadtripStoryteller/assets/5157474/7c8e714c-64a9-48ad-accb-fcff53c530be)

# Fred's Roadtrip Storyteller
Hear historical markers as you travel - Learn about local history as you drive.

- Compose KMP Proof-of-concept experimental app using Google Maps for iOS and Android
- Pure Kotlin App for Android and iOS

<!-- img width="1700" alt="image" src="https://github.com/realityexpander/GoolgeMapsUsingCocoaPodsExample/assets/5157474/7b0395d3-04e6-48df-9219-990cb10d81e1" -->

[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/db768efa-878e-47fe-96ac-bbdfadc5ad18">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/db768efa-878e-47fe-96ac-bbdfadc5ad18)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/9b896fbc-9799-4f0c-a642-9875c73575a8">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/9b896fbc-9799-4f0c-a642-9875c73575a8)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/92184386-c7fb-4ebe-8781-9d9b3e08efbd">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/92184386-c7fb-4ebe-8781-9d9b3e08efbd)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/d4f91c80-9d33-4063-bb37-85dc0f92fa26">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/d4f91c80-9d33-4063-bb37-85dc0f92fa26)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/784d6516-4d34-4478-a46a-35f368f1ebf0">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/784d6516-4d34-4478-a46a-35f368f1ebf0)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/c1894ee1-6ae3-4ed0-b5c2-b225a7f571ef">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/c1894ee1-6ae3-4ed0-b5c2-b225a7f571ef)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/265ae655-f57c-48a7-a60e-9a515d49efd3">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/265ae655-f57c-48a7-a60e-9a515d49efd3)
[<img width="250" alt="image" src="https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/ded7dd66-399b-41c7-9f74-00d8a27531dc">](https://github.com/realityexpander/FredsHistoryMarkers/assets/5157474/ded7dd66-399b-41c7-9f74-00d8a27531dc)

- Wiki: [Fred's Roadtrip Storyteller Wiki](https://github.com/realityexpander/FredsRoadtripStoryteller/wiki)

- Link to Marketing site source: https://github.com/realityexpander/FredsRoadtripStorytellerWebsite
- Link to Marketing Site: https://realityexpander.github.io/FredsRoadtripStorytellerWebsite/index.html

## Potential app names:
- https://www.mightynetworks.com/app-name-generator

icons: https://www.google.com/search?q=flat+icons+navigation&sca_esv=585465592&tbm=isch&sxsrf=AM9HkKkbHZOj7DIYI42VD56hpTfPgmYrYQ:1701032930698&source=lnms&sa=X&ved=2ahUKEwj_kte8yeKCAxVQIEQIHeeIDDsQ_AUoAXoECAIQAw&biw=1622&bih=1050&dpr=2#imgrc=Tx7XOXuIaxmJtM

Play store badge generator: https://play.google.com/intl/en_us/badges/

Privacy Policy: https://www.privacypolicygenerator.info/ 
https://www.privacypolicygenerator.info/download.php?lang=en&token=dhslNflkEHizHHbsBssrE8zM1p5AXwnR

## Developer notes

Firebase console: https://console.firebase.google.com/u/0/project/talkingmarkers/overview

Source: (Code snapshot taken 10/8/23)
https://github.com/JetBrains/compose-multiplatform/tree/master/examples/cocoapods-ios-example

- Must create AppSecrets.plist from within Xcode - in password vault
- Can get google-services.json from Google Cloud Console
- must do "pod install" from the iosApp folder

### Code style
  - https://github.com/realityexpander/FredsRoadtripStoryteller/blob/main/raw_assets/CodeStyle.md

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

- Screen Orientation
- https://youtrack.jetbrains.com/issue/KT-58292
- https://www.geeksforgeeks.org/detect-screen-orientation-in-android-using-jetpack-compose/
- https://codingwithrashid.com/how-to-get-screen-orientation-in-android-jetpack-compose/

- Build Config
- https://github.com/gmazzo/gradle-buildconfig-plugin

- Lottie for Compose (Android only?)
- https://github.com/airbnb/lottie/blob/master/android-compose.md

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

- Billing
- https://developer.android.com/google/play/billing/integrate
- https://medium.com/androiddevelopers/working-with-google-play-billing-part-2-b859b55426d2
- https://developer.android.com/google/play/billing/errors
- Nice example in compose: https://www.answertopia.com/jetpack-compose/a-jetpack-compose-in-app-purchasing-tutorial/
- Book from the author: https://www.ebookfrenzy.com/pdf_previews/JetpackCompose1.4EssentialsPreview.pdf
- Handling test licenses: https://play.google.com/console/u/2/developers/7466162782462237210/license-tester
- In-app products dashboard: https://play.google.com/console/u/2/developers/7466162782462237210/app/4975423493158681529/managed-products
- https://developer.android.com/google/play/billing/integrate
- https://reintech.io/blog/implementing-in-app-purchases-kotlin-google-play-billing-library
- https://support.google.com/googleplay/android-developer/answer/1153481?hl=en&sjid=4505455023269193106-NC#zippy=%2Ccreate-a-single-in-app-product
- https://www.namiml.com/blog/set-up-iap-google-play-android-app

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

- Maps stuff
- Override MKMapViewDelegateProtocol in Compose Multiplatform to receive pin selected events
- https://stackoverflow.com/questions/77266473/override-mkmapviewdelegateprotocol-in-compose-multiplatform-to-receive-pin-selec

- How to programmatically select annotation in Google Maps iOS?
- https://stackoverflow.com/questions/49156832/ios-google-map-programmatically-select-annotation-swift

- SwiftUI iOS 14 MapKit Annotation Tap Gesture (didSelect)
- https://developer.apple.com/forums/thread/659748

- Meet MapKit for SwiftUI
https://developer.apple.com/videos/play/wwdc2023/10043/

-[Solved] iOS 17 MapKit: How to use Map(selection:)?
-https://www.hackingwithswift.com/forums/swiftui/ios-17-mapkit-how-to-use-map-selection/22886

https://www.hackingwithswift.com/books/ios-swiftui/integrating-mapkit-with-swiftui


- STORE stuff
- StoreHelper Quick Start: https://github.com/russell-archer/StoreHelper/blob/main/Documentation/quickstart.md
- StoreHelper Guide https://github.com/russell-archer/StoreHelper/blob/main/Documentation/guide.md#get-started

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

- Material Design Palette
- https://materialpalettes.com/

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
