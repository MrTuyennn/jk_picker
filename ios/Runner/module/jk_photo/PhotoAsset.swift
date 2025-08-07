//
//  PhotoAsset.swift
//  Runner
//
//  Created by JunCook on 7/8/25.
//

import Photos
import os.log

/// Struct đại diện cho một ảnh hoặc video trong Photo Library
/// Wrap PHAsset để dễ sử dụng hơn và tích hợp tốt với SwiftUI
struct PhotoAsset: Identifiable {
    
    /// ID duy nhất cho SwiftUI Identifiable protocol
    /// Sử dụng identifier để đảm bảo tính duy nhất
    var id: String { identifier }
    
    /// Local identifier của PHAsset - định danh duy nhất trong Photo Library
    /// Được sử dụng để fetch lại asset từ Photo Library
    var identifier: String = UUID().uuidString
    
    /// Index của asset trong collection (có thể nil)
    /// Hữu ích cho việc hiển thị thứ tự trong UI
    var index: Int?
    
    /// PHAsset gốc từ Photos framework
    /// Chứa tất cả thông tin chi tiết của ảnh/video
    var phAsset: PHAsset?
    
    /// Type alias cho MediaType để dễ sử dụng
    typealias MediaType = PHAssetMediaType
    
    /// Kiểm tra xem asset có được đánh dấu favorite không
    /// - Returns: `true` nếu là favorite, `false` nếu không
    var isFavorite: Bool {
        phAsset?.isFavorite ?? false
    }
    
    /// Loại media của asset (photo, video, audio, unknown)
    /// - Returns: MediaType của asset hoặc .unknown nếu không có
    var mediaType: MediaType {
        phAsset?.mediaType ?? .unknown
    }
    
    /// Accessibility label cho VoiceOver
    /// Bao gồm thông tin về favorite status
    var accessibilityLabel: String {
        "Photo\(isFavorite ? ", Favorite" : "")"
    }

    /// Constructor tạo PhotoAsset từ PHAsset
    /// - Parameters:
    ///   - phAsset: PHAsset gốc từ Photos framework
    ///   - index: Index của asset trong collection (optional)
    init(phAsset: PHAsset, index: Int?) {
        self.phAsset = phAsset
        self.index = index
        // Sử dụng localIdentifier làm identifier duy nhất
        self.identifier = phAsset.localIdentifier
    }
    
    /// Constructor tạo PhotoAsset từ identifier
    /// Fetch PHAsset từ Photo Library dựa trên identifier
    /// - Parameter identifier: Local identifier của asset
    init(identifier: String) {
        self.identifier = identifier
        // Fetch asset từ Photo Library bằng identifier
        let fetchedAssets = PHAsset.fetchAssets(withLocalIdentifiers: [identifier], options: nil)
        self.phAsset = fetchedAssets.firstObject
    }
    
    /// Đặt trạng thái favorite cho asset
    /// Thực hiện thay đổi trong Photo Library
    /// - Parameter isFavorite: `true` để đánh dấu favorite, `false` để bỏ đánh dấu
    func setIsFavorite(_ isFavorite: Bool) async {
        guard let phAsset = phAsset else { return }
        
        Task {
            do {
                // Thực hiện thay đổi trong Photo Library
                try await PHPhotoLibrary.shared().performChanges {
                    let request = PHAssetChangeRequest(for: phAsset)
                    request.isFavorite = isFavorite
                }
            } catch (let error) {
                logger.error("Failed to change isFavorite: \(error.localizedDescription)")
            }
        }
    }
    
    /// Xóa asset khỏi Photo Library
    /// Thực hiện xóa vĩnh viễn asset
    func delete() async {
        guard let phAsset = phAsset else { return }
        
        do {
            // Thực hiện xóa asset trong Photo Library
            try await PHPhotoLibrary.shared().performChanges {
                PHAssetChangeRequest.deleteAssets([phAsset] as NSArray)
            }
            logger.debug("PhotoAsset asset deleted: \(index ?? -1)")
        } catch (let error) {
            logger.error("Failed to delete photo: \(error.localizedDescription)")
        }
    }
}

/// Extension để so sánh 2 PhotoAsset
/// So sánh dựa trên identifier và favorite status
extension PhotoAsset: Equatable {
    static func ==(lhs: PhotoAsset, rhs: PhotoAsset) -> Bool {
        (lhs.identifier == rhs.identifier) && (lhs.isFavorite == rhs.isFavorite)
    }
}

/// Extension để sử dụng PhotoAsset trong Set và Dictionary
/// Hash dựa trên identifier
extension PhotoAsset: Hashable {
    func hash(into hasher: inout Hasher) {
        hasher.combine(identifier)
    }
}

/// Extension để PHObject có thể sử dụng với SwiftUI Identifiable
/// Sử dụng localIdentifier làm id
extension PHObject: @retroactive Identifiable {
    public var id: String { localIdentifier }
}

/// Logger instance để log các thông báo liên quan đến PhotoAsset
fileprivate let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "PhotoAsset")

