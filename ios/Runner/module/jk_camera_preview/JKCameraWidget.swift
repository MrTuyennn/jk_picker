//
//  JKCameraWidget.swift
//  Runner
//
//  Created by JunCook on 31/7/25.
//

import Flutter
import Foundation
import UIKit
import SwiftUI


class JKCameraWidget: NSObject, FlutterPlatformView {
    
    private var hostingController: UIHostingController<JKCameraView>

    init(
        frame: CGRect, viewIdentifier viewId: Int64, arguments args: Any?,
        binaryMessenger messenger: FlutterBinaryMessenger?
    ) {
        let swiftUIView = JKCameraView()
        self.hostingController = UIHostingController(rootView: swiftUIView)
        self.hostingController.view.frame = frame
        super.init()

    }
    
    func view() -> UIView {
        return hostingController.view
    }
}
