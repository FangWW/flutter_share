import Flutter
import UIKit

public class SwiftFlutterSharePlugin: NSObject, FlutterPlugin {
    
    private var result: FlutterResult?
    private var viewController: UIViewController?
    var documentsUrl: URL {
        return FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
    }
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "flutter_share", binaryMessenger: registrar.messenger())
        let viewController: UIViewController? = UIApplication.shared.delegate?.window??.rootViewController
        let instance = SwiftFlutterSharePlugin(viewController: viewController)
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    init(viewController: UIViewController?) {
        super.init()
        
        self.viewController = viewController
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        
        if (self.result != nil) {
            self.result!(FlutterError(code: "multiple_request", message: "Cancelled by a second request", details: nil))
            self.result = nil
        }
        if ("share" == call.method) {
            
            self.result = result
            
            let args = call.arguments as? [String: Any?]
            
            let title = args!["title"] as? String
            //let message = args!["message"] as? String
            
            var sharedItems : Array<Any> = Array()
            
            //File url
            if let fileUrl = args!["fileUrl"] as? String, fileUrl.count > 0, let filePath = URL(string: fileUrl) {
                if let image = load(fileURL: filePath) {
                    sharedItems.append(image)
                } else {
                    sharedItems.append(filePath)
                }
            }
            
            let activityViewController = UIActivityViewController(activityItems: sharedItems, applicationActivities: nil)
            
            // Subject
            if (title != nil && title != "") {
                activityViewController.setValue(title, forKeyPath: "subject");
            }
            
            DispatchQueue.main.async {
                self.viewController?.present(activityViewController, animated: true, completion: nil)
            }
            
            result(true)
            
        } else {
            result(FlutterMethodNotImplemented)
        }
        
        
    }
    private func load(fileURL: URL) -> UIImage? {
        do {
            let imageData = try Data(contentsOf: fileURL)
            return UIImage(data: imageData)
        } catch {
            print("Error loading image : \(error)")
        }
        return nil
    }
}
