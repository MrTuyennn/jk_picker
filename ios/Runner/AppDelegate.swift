import Flutter
import UIKit

@main
@objc class AppDelegate: FlutterAppDelegate {
  override func application(
    _ application: UIApplication,
    didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?
  ) -> Bool {
      
      let controller: FlutterViewController =
          window?.rootViewController as! FlutterViewController
    
      // module jk_image_picker
      _ = JkImagePicker(controller: controller)
      
      weak var registerJKCamera = self.registrar(forPlugin: "jkcamera")
      let factoryJKCamera = JKCameraFactory(
          messenger: registerJKCamera!.messenger())
      
      self.registrar(forPlugin: "_jkcamera")!.register(
        factoryJKCamera, withId: AppConstant.CHANNEL_JK_CAMERA)
      
    GeneratedPluginRegistrant.register(with: self)
    return super.application(application, didFinishLaunchingWithOptions: launchOptions)
  }
}
