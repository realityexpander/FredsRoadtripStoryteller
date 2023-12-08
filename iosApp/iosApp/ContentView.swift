import GoogleMaps
import shared
import SwiftUI
import UIKit
// import MapKit

struct Location: Identifiable {
    let id = UUID()
    let name: String
    let coordinate: CLLocationCoordinate2D
}

struct ContentView: View {
    private var commonBilling: CommonBilling

    @StateObject
    private var entitlementManager: EntitlementManager

    @StateObject
    private var purchaseManager: PurchaseManager

    //  Leave for reference for now
    //   let cityHallLocation = CLLocationCoordinate2D(latitude: 37.779_379, longitude: -122.418_433)
    //   @State private var mapRegion = MKCoordinateRegion(center: CLLocationCoordinate2D(latitude: 51.5, longitude: -0.12), span: MKCoordinateSpan(latitudeDelta: 0.2, longitudeDelta: 0.2))
    //   let locations = [
    //       Location(name: "Buckingham Palace", coordinate: CLLocationCoordinate2D(latitude: 51.501, longitude: -0.141)),
    //       Location(name: "Tower of London", coordinate: CLLocationCoordinate2D(latitude: 51.508, longitude: -0.076))
    //   ]
    //   @State var position: MapCameraPosition = .automatic
    @State var selected: Int?

    init() {
        commonBilling = CommonBilling()

        let entitlementManager = EntitlementManager()
        let purchaseManager =
            PurchaseManager(
                entitlementManager: entitlementManager,
                commonBilling: commonBilling
            )
        _entitlementManager = StateObject(wrappedValue: entitlementManager)
        _purchaseManager = StateObject(wrappedValue: purchaseManager)

        commonBilling.commandFlow().watch { command in
            guard let command = command else { return }

            switch command {
            case is BillingCommand.Purchase:
                Task {
                    let productStr = (command as! BillingCommand.Purchase).productId
                    try? await purchaseManager.purchase(productStr)
                }
            case is BillingCommand.Consume:
                Task {
                    let productStr = (command as! BillingCommand.Purchase).productId
                    try? await purchaseManager.consume(productStr)
                }
            default:
                Task {
                    purchaseManager.purchaseCommandError("Unknown billing command")
                }
            }
        }
    }

    var body: some View {
        ZStack {
            Color.blue.ignoresSafeArea(.all) // status bar color
            ComposeView(
                commonBilling: commonBilling
            ).ignoresSafeArea(.all, edges: .bottom) // Compose has own keyboard handler

            // IOS Map experiments, leave for refernce.
           
//         Map(
//            coordinateRegion: MKCoordinateRegion(
//               center: CLLocationCoordinate2D(latitude: 37.779_379, longitude: -122.418_433),
//               latitudinalMeters: CLLocationDistance(1000),
//               longitudinalMeters: CLLocationDistance(1000)
//            )
//         )
//       }

            // iOS 15 map
//         Map(
//            coordinateRegion: $mapRegion,
//            interactionModes: MapInteractionModes.all,
//             annotationItems: locations
//         ) { location in
            ////            MapMarker(
            ////               coordinate: CLLocationCoordinate2D(latitude: 37.7793, longitude: -122.416),
            ////               tint:Color.red
            ////            )
            ////            MapMarker(coordinate: location.coordinate)
            ////            MapPin(coordinate: location.coordinate)
//
//
            ////            MapAnnotation(coordinate: location.coordinate) {
            ////               NavigationLink {
            ////                  Text(location.name)
            ////               } label: {
            ////                  Text(location.name)
            ////                     .onTapGesture {
            ////                        print("Fuck off \(location.name)")
            ////                     }
            ////                     .foregroundColor(Color.white)
            ////
            ////
            //////                     .fontDesign(Font.system(size: 15.0))
            //////                  Circle()
            //////                     .stroke(.red, lineWidth: 3)
            //////                     .frame(width: 44, height: 44)
            ////               }
            ////            }
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

//         Button(action: {
//            selected = 1
//         }) {
//            if(selected != nil) {
//               Text("Selected: \(selected!)")
//            } else {
//               Text("Selected: no")
//            }
//         }
        }
        .preferredColorScheme(.dark)
        .task {
            _ = Task<Void, Never> {
                do {
                    try await purchaseManager.loadProducts()
                } catch {
                    print(error)
                }
            }
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    private var commonBilling: CommonBilling

    init(commonBilling: CommonBilling) {
        self.commonBilling = commonBilling
    }

    func makeUIViewController(context: Context) -> UIViewController {
        // Load the Google maps API key from the AppSecrets.plist file
        let filePath = Bundle.main.path(forResource: "AppSecrets", ofType: "plist")!
        let plist = NSDictionary(contentsOfFile: filePath)!
        let googleMapsApiKey = plist["GOOGLE_MAPS_API_KEY"] as! String

        GMSServices.provideAPIKey(googleMapsApiKey)

        // Start the App
        return Main_iosKt.MainViewController(commonBilling: commonBilling)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
