import GoogleMaps
import shared
import SwiftUI
import UIKit

struct ContentView: View {
    private var commonAppMetadata: CommonAppMetadata
    
    private let commonBilling: CommonBilling = CommonBilling()
    @StateObject
    private var entitlementManager: EntitlementManager
    @StateObject
    private var purchaseManager: PurchaseManager
    
    private var commonSpeech: CommonSpeech = CommonSpeech()
    private var textToSpeechManager: TextToSpeechManager
    
    init() {
        // Set the app meta info
        let nsObject: AnyObject? = Bundle.main.infoDictionary!["CFBundleShortVersionString"] as AnyObject
        let version = nsObject as! String
        let nsObject2: AnyObject? = Bundle.main.infoDictionary!["CFBundleVersion"] as AnyObject
        let bundleVersion = nsObject2 as! String
        #if DEBUG
            let isDebuggable = true
        #else
            let isDebuggable = false
        #endif
        commonAppMetadata = CommonAppMetadata(
            isDebuggable: isDebuggable,
            versionStr: version,
            androidBuildNumberStr: "n/a", // Android only
            iOSBundleVersionStr: bundleVersion,
            installAtEpochMilli: 0,
            platformId: "iOS"
        )

        // Setup speech
        textToSpeechManager = TextToSpeechManager(commonSpeech: commonSpeech)

        // Setup billing
        let entitlementManager = EntitlementManager()
        let purchaseManager =
            PurchaseManager(
                entitlementManager: entitlementManager,
                commonBilling: commonBilling
            )
        _entitlementManager = StateObject(wrappedValue: entitlementManager)
        _purchaseManager = StateObject(wrappedValue: purchaseManager)
    }
    
    func listenToCommonBillingCommandFlow() {
        commonBilling.commandFlow().watch { command in
            guard let command = command else { return }

            switch command {
            case is CommonBilling.BillingCommandPurchase:
                Task {
                    let productStr = (command as! CommonBilling.BillingCommandPurchase).productId
                    try? await self.purchaseManager.purchase(productStr)
                }
            case is CommonBilling.BillingCommandConsume:
                Task {
                    let productStr = (command as! CommonBilling.BillingCommandPurchase).productId
                    try? await self.purchaseManager.consume(productStr)
                }
            default:
                Task {
                    self.purchaseManager.purchaseCommandError("Unknown billing command")
                }
            }
        }
    }
    
    func listenToCommonSpeechSpeakTextCommandFlow() {
        commonSpeech.speakTextCommandCommonFlow().watch { text in
            print("Speak: \(text ?? "") ")
            guard let text2 = text else { return }
            Task {
                textToSpeechManager.speakText(text: text2 as String)
            }
        }
    }

    var body: some View {
        ZStack {
            Color.blue.ignoresSafeArea(.all) // status bar color
            ComposeView(
                commonAppMetadata: commonAppMetadata,
                commonBilling: commonBilling,
                commonSpeech: commonSpeech
            ).ignoresSafeArea(.all, edges: .bottom) // Compose has own keyboard handler

            // IOS Map experiments extracted from here, LEAVE FOR REFERENCE.
        }
        .preferredColorScheme(.dark)
        .task {
            _ = Task<Void, Never> {
                do {
                    try await purchaseManager.loadProducts()
                    listenToCommonBillingCommandFlow()
                    listenToCommonSpeechSpeakTextCommandFlow()
                } catch {
                    print(error)
                }
            }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    private var commonAppMetadata: CommonAppMetadata
    private var commonBilling: CommonBilling
    private var commonSpeech: CommonSpeech

    init(
        commonAppMetadata: CommonAppMetadata,
        commonBilling: CommonBilling,
        commonSpeech: CommonSpeech
    ) {
        self.commonAppMetadata = commonAppMetadata
        self.commonBilling = commonBilling
        self.commonSpeech = commonSpeech
    }

    func makeUIViewController(context: Context) -> UIViewController {
        // Load the Google maps API key from the AppSecrets.plist file
        let filePath = Bundle.main.path(forResource: "AppSecrets", ofType: "plist")!
        let plist = NSDictionary(contentsOfFile: filePath)!
        let googleMapsApiKey = plist["GOOGLE_MAPS_API_KEY"] as! String

        GMSServices.provideAPIKey(googleMapsApiKey)

        // Start the App
        return Main_iosKt.MainViewController(
            commonBilling: commonBilling,
            commonAppMetadata: commonAppMetadata,
            commonSpeech: commonSpeech
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}















// import MapKit

//  Alternate Maps implementation - LEAVE FOR REFERENCE for now
//struct Location: Identifiable {
//    let id = UUID()
//    let name: String
//    let coordinate: CLLocationCoordinate2D
//}

//  Alternate Maps implementation - LEAVE FOR REFERENCE for now
//   let cityHallLocation = CLLocationCoordinate2D(latitude: 37.779_379, longitude: -122.418_433)
//   @State private var mapRegion = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 51.5, longitude: -0.12), span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2))
//   let locations = [
//       Location(name: "Buckingham Palace", coordinate: CLLocationCoordinate2D(latitude: 51.501, longitude: -0.141)),
//       Location(name: "Tower of London", coordinate: CLLocationCoordinate2D(latitude: 51.508, longitude: -0.076))
//   ]
//   @State var position: MapCameraPosition = .automatic
//   @State var selected: Int?

//          // IOS Map experiments, LEAVE FOR REFERENCE.
//         Map(
//            coordinateRegion: MKCoordinateRegion(
//               center: CLLocationCoordinate2D(latitude: 37.779_379, longitude: -122.418_433),
//               latitudinalMeters: CLLocationDistance(1000),
//               longitudinalMeters: CLLocationDistance(1000)
//            )
//         )
//       }

//          // iOS 15 map
//         Map(
//            coordinateRegion: $mapRegion,
//            interactionModes: MapInteractionModes.all,
//             annotationItems: locations
//         ) { location in
//            ////            MapMarker(
//            ////               coordinate: CLLocationCoordinate2D(latitude: 37.7793, longitude: -122.416),
//            ////               tint:Color.red
//            ////            )
//            ////            MapMarker(coordinate: location.coordinate)
//            ////            MapPin(coordinate: location.coordinate)
//            ////            MapAnnotation(coordinate: location.coordinate) {
//            ////               NavigationLink {
//            ////                  Text(location.name)
//            ////               } label: {
//            ////                  Text(location.name)
//            ////                     .onTapGesture {
//            ////                        print("Fuck off \(location.name)")
//            ////                     }
//            ////                     .foregroundColor(Color.white)
//            ////
//            ////
//            //////                     .fontDesign(Font.system(size: 15.0))
//            //////                  Circle()
//            //////                     .stroke(.red, lineWidth: 3)
//            //////                     .frame(width: 44, height: 44)
//            ////               }
//            ////            }
//
//            MapAnnotation(coordinate: location.coordinate) {
//               Text(location.name)
//                  .foregroundColor(Color.white)
//
//                Circle()
//                    .stroke(.red, lineWidth: 3)
//                    .frame(width: 44, height: 44)
//                    .onTapGesture {
//                        print("Tapped on \(location.name)")
//                    }
//                    .onAppear {
//                       print("OnAppear")
//                    }
//            }
//
//        }.frame(height: 300)

// iOS 17
// https://www.hackingwithswift.com/forums/swiftui/ios-17-mapkit-how-to-use-map-selection/22886
// https://medium.com/simform-engineering/mapkit-swiftui-in-ios-17-1fec82c3bf00
// https://www.kodeco.com/7738344-mapkit-tutorial-getting-started?page=4#toc-anchor-020
// https://www.kodeco.com/40607811-new-swiftui-support-for-mapkit-in-xcode-15?page=1#toc-anchor-005
//          Map(position: $position, selection: $selected) {
//             Marker("test", coordinate: locations[0].coordinate).tag(1)
//             Marker("test2", coordinate: locations[1].coordinate).tag(2)
//          }
//          .position(CGPoint(x:200.0, y:-40.0))
//          //.offset(CGSize(width: 0.0, height: -100.0))
//          .frame(height: 300, alignment: .top)
//          .safeAreaInset(edge: .bottom) {
//             if let selected {
//                Text("Selected: \(selected)")
//                     .frame(height: 128)
//                     .clipShape(RoundedRectangle(cornerRadius: 10))
//                     .padding([.top, .horizontal])
//             } else {
//                VStack {
//                   Text("Selected: no")
//
//                   Button(action: {
//                      selected = 1
//                   }) {
//                      if(selected != nil) {
//                         Text("Selected: \(selected!)")
//                      } else {
//                         Text("Selected: no")
//                      }
//                   }
//                }
//             }
//         }
//
//         Button(action: {
//            selected = 1
//         }) {
//            if(selected != nil) {
//               Text("Selected: \(selected!)")
//            } else {
//               Text("Selected: no")
//            }
//         }

