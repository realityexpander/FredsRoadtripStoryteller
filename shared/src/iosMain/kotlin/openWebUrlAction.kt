// iOS
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import co.touchlab.kermit.Logger as Log

actual fun openWebUrlAction(urlStr: String) {
    Log.d("openWebUrl: url: $urlStr") // todo implement for ios
    val url = NSURL(string = urlStr)

    if (UIApplication.sharedApplication.canOpenURL(url)) {
        UIApplication.sharedApplication.openURL(url)
    }
}
