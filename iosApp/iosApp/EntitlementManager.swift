import SwiftUI

class EntitlementManager: ObservableObject {
    static let userDefaults = UserDefaults(suiteName: "group.com.realityexpander.talkinghistoricalmarkers")

    @AppStorage("isProPurchased", store: userDefaults)
    var isProPurchased: Bool = false
}
