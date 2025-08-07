//
//  PhotoCollection.swift
//  Runner
//
//  Created by JunCook on 7/8/25.
//
import Photos
import os.log

/// Class quản lý collection của ảnh/video trong Photo Library
/// Có thể quản lý album thường hoặc smart album
/// Implement ObservableObject để tích hợp với SwiftUI
/// Implement PHPhotoLibraryChangeObserver để tự động update khi có thay đổi
class PhotoCollection: NSObject, ObservableObject {
    
    /// Published property chứa collection của PhotoAsset
    /// SwiftUI sẽ tự động update UI khi property này thay đổi
    @Published var photoAssets: PhotoAssetCollection = PhotoAssetCollection(PHFetchResult<PHAsset>())
    
    /// Local identifier của album (có thể nil)
    /// Được sử dụng để fetch lại album từ Photo Library
    var identifier: String? {
        assetCollection?.localIdentifier
    }
    
    /// Tên của album (có thể nil cho smart album)
    var albumName: String?
    
    /// Loại smart album (có thể nil cho album thường)
    /// Ví dụ: .smartAlbumUserLibrary, .smartAlbumFavorites, etc.
    var smartAlbumType: PHAssetCollectionSubtype?
    
    /// Cache manager để quản lý việc load và cache ảnh
    /// Giúp tăng performance khi hiển thị ảnh
    let cache = CachedImageManager()
    
    /// PHAssetCollection gốc từ Photos framework
    /// Chứa thông tin chi tiết của album
    private var assetCollection: PHAssetCollection?
    
    /// Flag để tạo album mới nếu không tìm thấy
    /// Mặc định là false
    private var createAlbumIfNotFound = false
    
    /// Enum định nghĩa các loại lỗi có thể xảy ra
    /// Implement LocalizedError để có thể hiển thị thông báo lỗi
    enum PhotoCollectionError: LocalizedError {
        case missingAssetCollection      // Không có asset collection
        case missingAlbumName           // Không có tên album
        case missingLocalIdentifier     // Không có local identifier
        case unableToFindAlbum(String)  // Không tìm thấy album
        case unableToLoadSmartAlbum(PHAssetCollectionSubtype)  // Không load được smart album
        case addImageError(Error)       // Lỗi khi thêm ảnh
        case createAlbumError(Error)    // Lỗi khi tạo album
        case removeAllError(Error)      // Lỗi khi xóa tất cả
    }
    
    /// Constructor tạo PhotoCollection từ tên album
    /// - Parameters:
    ///   - albumName: Tên album cần load
    ///   - createIfNotFound: Có tạo album mới nếu không tìm thấy không
    init(albumNamed albumName: String, createIfNotFound: Bool = false) {
        self.albumName = albumName
        self.createAlbumIfNotFound = createIfNotFound
        super.init()
    }

    /// Constructor tạo PhotoCollection từ identifier
    /// - Parameter identifier: Local identifier của album
    /// - Returns: PhotoCollection hoặc nil nếu không tìm thấy album
    init?(albumWithIdentifier identifier: String) {
        guard let assetCollection = PhotoCollection.getAlbum(identifier: identifier) else {
            logger.error("Photo album not found for identifier: \(identifier)")
            return nil
        }
        logger.log("Loaded photo album with identifier: \(identifier)")
        self.assetCollection = assetCollection
        super.init()
        Task {
            await refreshPhotoAssets()
        }
    }
    
    /// Constructor tạo PhotoCollection từ smart album
    /// - Parameter smartAlbumType: Loại smart album cần load
    init(smartAlbum smartAlbumType: PHAssetCollectionSubtype) {
        self.smartAlbumType = smartAlbumType
        super.init()
    }
    
    /// Deinit để unregister observer khi object bị deallocate
    deinit {
        PHPhotoLibrary.shared().unregisterChangeObserver(self)
    }
    
    /// Load album hoặc smart album từ Photo Library
    /// Function này sẽ được gọi để khởi tạo album
    /// - Throws: PhotoCollectionError nếu có lỗi xảy ra
    func load() async throws {
        
        // Đăng ký observer để lắng nghe thay đổi trong Photo Library
        PHPhotoLibrary.shared().register(self)
        
        // Xử lý smart album
        if let smartAlbumType = smartAlbumType {
            if let assetCollection = PhotoCollection.getSmartAlbum(subtype: smartAlbumType) {
                logger.log("Loaded smart album of type: \(smartAlbumType.rawValue)")
                self.assetCollection = assetCollection
                await refreshPhotoAssets()
                return
            } else {
                logger.error("Unable to load smart album of type: : \(smartAlbumType.rawValue)")
                throw PhotoCollectionError.unableToLoadSmartAlbum(smartAlbumType)
            }
        }
        
        // Kiểm tra tên album
        guard let name = albumName, !name.isEmpty else {
            logger.error("Unable to load an album without a name.")
            throw PhotoCollectionError.missingAlbumName
        }
        
        // Thử load album từ tên
        if let assetCollection = PhotoCollection.getAlbum(named: name) {
            logger.log("Loaded photo album named: \(name)")
            self.assetCollection = assetCollection
            await refreshPhotoAssets()
            return
        }
        
        // Kiểm tra có tạo album mới không
        guard createAlbumIfNotFound else {
            logger.error("Unable to find photo album named: \(name)")
            throw PhotoCollectionError.unableToFindAlbum(name)
        }

        logger.log("Creating photo album named: \(name)")
        
        // Tạo album mới
        if let assetCollection = try? await PhotoCollection.createAlbum(named: name) {
            self.assetCollection = assetCollection
            await refreshPhotoAssets()
        }
    }
    
    /// Thêm ảnh vào album
    /// - Parameter imageData: Data của ảnh cần thêm
    /// - Throws: PhotoCollectionError nếu có lỗi xảy ra
    func addImage(_ imageData: Data) async throws {
        guard let assetCollection = self.assetCollection else {
            throw PhotoCollectionError.missingAssetCollection
        }
        
        do {
            // Thực hiện thay đổi trong Photo Library
            try await PHPhotoLibrary.shared().performChanges {
                
                // Tạo request tạo asset mới
                let creationRequest = PHAssetCreationRequest.forAsset()
                if let assetPlaceholder = creationRequest.placeholderForCreatedAsset {
                    // Thêm resource (ảnh) vào asset
                    creationRequest.addResource(with: .photo, data: imageData, options: nil)
                    
                    // Thêm asset vào album
                    if let albumChangeRequest = PHAssetCollectionChangeRequest(for: assetCollection), assetCollection.canPerform(.addContent) {
                        let fastEnumeration = NSArray(array: [assetPlaceholder])
                        albumChangeRequest.addAssets(fastEnumeration)
                    }
                }
            }
            
            // Refresh collection sau khi thêm
            await refreshPhotoAssets()
            
        } catch let error {
            logger.error("Error adding image to photo library: \(error.localizedDescription)")
            throw PhotoCollectionError.addImageError(error)
        }
    }
    
    /// Xóa một asset khỏi album
    /// - Parameter asset: PhotoAsset cần xóa
    /// - Throws: PhotoCollectionError nếu có lỗi xảy ra
    func removeAsset(_ asset: PhotoAsset) async throws {
        guard let assetCollection = self.assetCollection else {
            throw PhotoCollectionError.missingAssetCollection
        }
        
        do {
            // Thực hiện xóa asset khỏi album
            try await PHPhotoLibrary.shared().performChanges {
                if let albumChangeRequest = PHAssetCollectionChangeRequest(for: assetCollection) {
                    albumChangeRequest.removeAssets([asset as Any] as NSArray)
                }
            }
            
            // Refresh collection sau khi xóa
            await refreshPhotoAssets()
            
        } catch let error {
            logger.error("Error removing all photos from the album: \(error.localizedDescription)")
            throw PhotoCollectionError.removeAllError(error)
        }
    }
    
    /// Xóa tất cả asset khỏi album
    /// - Throws: PhotoCollectionError nếu có lỗi xảy ra
    func removeAll() async throws {
        guard let assetCollection = self.assetCollection else {
            throw PhotoCollectionError.missingAssetCollection
        }
        
        do {
            // Thực hiện xóa tất cả asset khỏi album
            try await PHPhotoLibrary.shared().performChanges {
                if let albumChangeRequest = PHAssetCollectionChangeRequest(for: assetCollection),
                    let assets = (PHAsset.fetchAssets(in: assetCollection, options: nil) as AnyObject?) as! PHFetchResult<AnyObject>? {
                    albumChangeRequest.removeAssets(assets)
                }
            }
            
            // Refresh collection sau khi xóa
            await refreshPhotoAssets()
            
        } catch let error {
            logger.error("Error removing all photos from the album: \(error.localizedDescription)")
            throw PhotoCollectionError.removeAllError(error)
        }
    }
    
    /// Refresh collection của PhotoAsset
    /// Function này sẽ fetch lại data từ Photo Library và update photoAssets
    /// - Parameter fetchResult: PHFetchResult mới (optional)
    private func refreshPhotoAssets(_ fetchResult: PHFetchResult<PHAsset>? = nil) async {

        var newFetchResult = fetchResult

        // Nếu không có fetchResult được truyền vào, tạo mới
        if newFetchResult == nil {
            let fetchOptions = PHFetchOptions()
            // chỉ lấy image
            fetchOptions.predicate = NSPredicate(format: "mediaType == %d", PHAssetMediaType.image.rawValue)
            // Sắp xếp theo ngày tạo, mới nhất lên đầu
            fetchOptions.sortDescriptors = [NSSortDescriptor(key: "creationDate", ascending: false)]
            if let assetCollection = self.assetCollection, let fetchResult = (PHAsset.fetchAssets(in: assetCollection, options: fetchOptions) as AnyObject?) as? PHFetchResult<PHAsset> {
                newFetchResult = fetchResult
                
                // Log chi tiết về data được fetch
                print("📸 === PHOTO DATA FETCHED ===")
                print("📊 Total assets found: \(fetchResult.count)")
                
                // Log từng asset để xem data
                for i in 0..<min(fetchResult.count, 10) { // Chỉ log 10 items đầu để tránh spam
                    let asset = fetchResult.object(at: i)
                    print("📷 Asset \(i):")
                    print("   - ID: \(asset.localIdentifier)")
                    print("   - Media Type: \(asset.mediaType.rawValue)")
                    print("   - Creation Date: \(asset.creationDate?.description ?? "Unknown")")
                    print("   - Duration: \(asset.duration) seconds")
                    print("   - Favorite: \(asset.isFavorite)")
                    print("   - Location: \(asset.location?.description ?? "No location")")
                  //  print("   - File Size: \(asset.value(forKey: "fileSize") ?? "Unknown") bytes")
                }
                
                if fetchResult.count > 10 {
                    print("📷 ... and \(fetchResult.count - 10) more assets")
                }
                print("📸 === END PHOTO DATA ===")
            }
        }
        
        // Update photoAssets trên main thread
        if let newFetchResult = newFetchResult {
            await MainActor.run {
                photoAssets = PhotoAssetCollection(newFetchResult)
                logger.debug("PhotoCollection photoAssets refreshed: \(self.photoAssets.count)")
            }
        }
    }

    /// Helper function để lấy album từ identifier
    /// - Parameter identifier: Local identifier của album
    /// - Returns: PHAssetCollection hoặc nil
    private static func getAlbum(identifier: String) -> PHAssetCollection? {
        let fetchOptions = PHFetchOptions()
        let collections = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [identifier], options: fetchOptions)
        return collections.firstObject
    }
    
    /// Helper function để lấy album từ tên
    /// - Parameter name: Tên album cần tìm
    /// - Returns: PHAssetCollection hoặc nil
    private static func getAlbum(named name: String) -> PHAssetCollection? {
        let fetchOptions = PHFetchOptions()
        // Tìm album có title trùng với name
        fetchOptions.predicate = NSPredicate(format: "title = %@", name)
        let collections = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: fetchOptions)
        return collections.firstObject
    }
    
    /// Helper function để lấy smart album
    /// - Parameter subtype: Loại smart album
    /// - Returns: PHAssetCollection hoặc nil
    private static func getSmartAlbum(subtype: PHAssetCollectionSubtype) -> PHAssetCollection? {
        let fetchOptions = PHFetchOptions()
        let collections = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: subtype, options: fetchOptions)
        return collections.firstObject
    }
    
    /// Helper function để tạo album mới
    /// - Parameter name: Tên album cần tạo
    /// - Returns: PHAssetCollection mới được tạo
    /// - Throws: PhotoCollectionError nếu có lỗi
    private static func createAlbum(named name: String) async throws -> PHAssetCollection? {
        var collectionPlaceholder: PHObjectPlaceholder?
        do {
            // Tạo album mới trong Photo Library
            try await PHPhotoLibrary.shared().performChanges {
                let createAlbumRequest = PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: name)
                collectionPlaceholder = createAlbumRequest.placeholderForCreatedAssetCollection
            }
        } catch let error {
            logger.error("Error creating album in photo library: \(error.localizedDescription)")
            throw PhotoCollectionError.createAlbumError(error)
        }
        logger.log("Created photo album named: \(name)")
        
        // Lấy album vừa tạo bằng identifier
        guard let collectionIdentifier = collectionPlaceholder?.localIdentifier else {
            throw PhotoCollectionError.missingLocalIdentifier
        }
        let collections = PHAssetCollection.fetchAssetCollections(withLocalIdentifiers: [collectionIdentifier], options: nil)
        return collections.firstObject
    }
}

/// Extension để implement PHPhotoLibraryChangeObserver
/// Tự động update khi có thay đổi trong Photo Library
extension PhotoCollection: PHPhotoLibraryChangeObserver {
    
    /// Callback được gọi khi có thay đổi trong Photo Library
    /// - Parameter changeInstance: Instance chứa thông tin thay đổi
    func photoLibraryDidChange(_ changeInstance: PHChange) {
        Task { @MainActor in
            // Kiểm tra xem có thay đổi trong fetchResult không
            guard let changes = changeInstance.changeDetails(for: self.photoAssets.fetchResult) else { return }
            // Refresh với fetchResult mới
            await self.refreshPhotoAssets(changes.fetchResultAfterChanges)
        }
    }
}

/// Logger instance để log các thông báo liên quan đến PhotoCollection
fileprivate let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "PhotoCollection")
