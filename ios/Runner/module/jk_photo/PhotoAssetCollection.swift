//
//  PhotoAssetCollection.swift
//  Runner
//
//  Created by JunCook on 7/8/25.
//

import Photos

import Photos

/// Class wrapper cho PHFetchResult để dễ sử dụng với SwiftUI
/// Implement RandomAccessCollection để có thể sử dụng như array
/// Có cache để tăng performance khi access các PhotoAsset
class PhotoAssetCollection: RandomAccessCollection {
    
    /// PHFetchResult gốc từ Photos framework
    /// Chứa tất cả PHAsset được fetch từ Photo Library
    private(set) var fetchResult: PHFetchResult<PHAsset>
    
    /// Index hiện tại cho iterator
    /// Được sử dụng trong IteratorProtocol
    private var iteratorIndex: Int = 0
    
    /// Cache các PhotoAsset đã được tạo
    /// Key: index position, Value: PhotoAsset
    /// Giúp tăng performance khi access lại cùng một asset
    private var cache = [Int : PhotoAsset]()
    
    /// Index bắt đầu của collection (luôn là 0)
    var startIndex: Int { 0 }
    
    /// Index kết thúc của collection (số lượng asset)
    var endIndex: Int { fetchResult.count }
    
    /// Constructor tạo PhotoAssetCollection từ PHFetchResult
    /// - Parameter fetchResult: PHFetchResult chứa các PHAsset
    init(_ fetchResult: PHFetchResult<PHAsset>) {
        self.fetchResult = fetchResult
    }

    /// Subscript để access PhotoAsset bằng index
    /// Có cache để tăng performance
    /// - Parameter position: Index của asset cần lấy
    /// - Returns: PhotoAsset tại vị trí position
    subscript(position: Int) -> PhotoAsset {
        // Kiểm tra cache trước
        if let asset = cache[position] {
            return asset
        }
        
        // Tạo PhotoAsset mới nếu chưa có trong cache
        let asset = PhotoAsset(phAsset: fetchResult.object(at: position), index: position)
        cache[position] = asset
        return asset
    }
    
    /// Lấy array tất cả PHAsset từ fetchResult
    /// - Returns: Array PHAsset
    var phAssets: [PHAsset] {
        var assets = [PHAsset]()
        // Enumerate qua tất cả PHAsset trong fetchResult
        fetchResult.enumerateObjects { (object, count, stop) in
            assets.append(object)
        }
        return assets
    }
}

/// Extension để PhotoAssetCollection có thể sử dụng như Sequence
/// Implement IteratorProtocol để có thể iterate qua collection
extension PhotoAssetCollection: Sequence, IteratorProtocol {

    /// Function next() cho IteratorProtocol
    /// Trả về PhotoAsset tiếp theo hoặc nil nếu đã hết
    /// - Returns: PhotoAsset tiếp theo hoặc nil
    func next() -> PhotoAsset? {
        // Kiểm tra xem đã hết collection chưa
        if iteratorIndex >= count {
            return nil
        }
        
        // Tăng index cho lần next tiếp theo
        defer {
            iteratorIndex += 1
        }
        
        // Trả về asset tại vị trí hiện tại
        return self[iteratorIndex]
    }
}
