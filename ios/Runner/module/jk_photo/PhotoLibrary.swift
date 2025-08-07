//
//  PhotoLibary.swift
//  Runner
//
//  Created by JunCook on 7/8/25.
//

import Photos
import os.log

/// Class quản lý quyền truy cập Photo Library
/// Cung cấp các function để kiểm tra và yêu cầu quyền truy cập ảnh/video
class PhotoLibrary {
    
    /// Kiểm tra và yêu cầu quyền truy cập Photo Library
    /// - Returns: `true` nếu được cấp quyền, `false` nếu bị từ chối hoặc bị hạn chế
    /// - Note: Function này sẽ hiển thị dialog yêu cầu quyền nếu chưa được xác định
    static func checkAuthorization() async -> Bool {
        // Kiểm tra trạng thái quyền truy cập hiện tại
        switch PHPhotoLibrary.authorizationStatus(for: .readWrite) {
        case .authorized:
            // Đã được cấp quyền đầy đủ - có thể đọc và ghi
            logger.debug("Photo library access authorized.")
            return true
            
        case .notDetermined:
            // Chưa xác định - cần yêu cầu quyền từ user
            logger.debug("Photo library access not determined.")
            // Hiển thị dialog yêu cầu quyền và đợi user response
            return await PHPhotoLibrary.requestAuthorization(for: .readWrite) == .authorized
            
        case .denied:
            // User đã từ chối quyền truy cập
            logger.debug("Photo library access denied.")
            return false
            
        case .limited:
            // Quyền bị giới hạn (iOS 14+) - chỉ có thể truy cập một số ảnh được chọn
            logger.debug("Photo library access limited.")
            return false
            
        case .restricted:
            // Quyền bị hạn chế bởi parental controls hoặc MDM
            logger.debug("Photo library access restricted.")
            return false
            
        @unknown default:
            // Trường hợp mới có thể được thêm trong tương lai
            return false
        }
    }
}

/// Logger instance để log các thông báo liên quan đến Photo Library
/// Sử dụng os.log framework để log có cấu trúc
fileprivate let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "PhotoLibrary")
