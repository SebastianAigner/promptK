import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

actual fun go(address: String) {
    val activityController = UIActivityViewController(
        activityItems = listOf(address),
        applicationActivities = null
    )

    // If we were a Composable function, we could use LocalUIViewController.current.
    val viewController = UIApplication.sharedApplication.keyWindow!!.rootViewController!!
    activityController.popoverPresentationController?.sourceView = viewController.view
    activityController.setTitle("Hello there")

    viewController.presentViewController(activityController, animated = true, completion = {})
}
