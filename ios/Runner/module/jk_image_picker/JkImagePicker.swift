//
//  JkImagePicker.swift
//  Runner
//
//  Created by JunCook on 29/7/25.
//

import Foundation
import Flutter
import UIKit
import Photos

class JkImagePicker {
    
    var controller: FlutterViewController
    private var methodChannel: FlutterMethodChannel
    
    init(controller: FlutterViewController){
        self.controller = controller
        self.methodChannel = FlutterMethodChannel(
            name: AppConstant.CHANNEL_JK_PICKER,
            binaryMessenger: controller.binaryMessenger)
        self.initMethodChannel()
    }
    
    
    private func initMethodChannel() {
        methodChannel.setMethodCallHandler({
            (call:FlutterMethodCall, result: @escaping FlutterResult) in
            if call.method == AppConstant.METHOD_GET_ALBUMS {
                self.checkPhotoPermission { granted in
                            if granted {
                                        result(self.fetchAlbums())
                                    } else {
                                        result(FlutterError(code: "PERMISSION_DENIED", message: "Photo permission not granted", details: nil))
                                    }
                }
            } else if call.method == AppConstant.METHOD_GET_ASSETSINALBUM {
                
            } else if call.method == AppConstant.METHOD_GET_IMAGE {
                
            } else {
                result(FlutterMethodNotImplemented)
                return
            }
        })
    }
    
    private func fetchAlbums() -> [[String: Any]] {
        var list: [[String: Any]] = []
        let options = PHFetchOptions()
           // Smart Albums (Recents, Favorites, Camera Roll)
           let smartAlbums = PHAssetCollection.fetchAssetCollections(with: .smartAlbum, subtype: .any, options: options)
           smartAlbums.enumerateObjects { col, _, _ in
               print(col)
               let assets = PHAsset.fetchAssets(in: col, options: nil)
               if assets.count > 0 {
                   list.append(["id": col.localIdentifier, "name": col.localizedTitle ?? "Album"])
               }
           }
           
           // User Albums
           let userAlbums = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: options)
//           print(userAlbums)
           userAlbums.enumerateObjects { col, _, _ in
               let assets = PHAsset.fetchAssets(in: col, options: nil)
               if assets.count > 0 {
                   list.append(["id": col.localIdentifier, "name": col.localizedTitle ?? "Album"])
               }
           }
           
           return list
    }

    private func fetchAssets(albumId: String) -> [[String: Any]] {
        var list: [[String: Any]] = []
        if let collection = PHAssetCollection.fetchAssetCollections(
            withLocalIdentifiers: [albumId],
            options: nil
        ).firstObject {
            let fetchOptions = PHFetchOptions()
            fetchOptions.sortDescriptors = [
                NSSortDescriptor(key: "creationDate", ascending: false)
            ]
            let assets = PHAsset.fetchAssets(
                in: collection,
                options: fetchOptions
            )
            assets.enumerateObjects { asset, _, _ in
                list.append([
                    "id": asset.localIdentifier,
                    "mediaType": asset.mediaType.rawValue,
                ])
            }
        }
        return list
    }

    private func fetchImage(
        assetId: String,
        completion: @escaping (UIImage) -> Void
    ) {
        let assets = PHAsset.fetchAssets(
            withLocalIdentifiers: [assetId],
            options: nil
        )
        guard let asset = assets.firstObject else { return }
        let manager = PHImageManager.default()
        manager.requestImage(
            for: asset,
            targetSize: CGSize(width: 500, height: 500),
            contentMode: .aspectFill,
            options: nil
        ) { image, _ in
            if let img = image {
                completion(img)
            }
        }
    }
    
    // MARK: - Permission
       private func checkPhotoPermission(completion: @escaping (Bool) -> Void) {
           let status = PHPhotoLibrary.authorizationStatus()
           if #available(iOS 14, *) {
               if status == .authorized || status == .limited {
                   completion(true)
               } else if status == .notDetermined {
                   PHPhotoLibrary.requestAuthorization { newStatus in
                       DispatchQueue.main.async {
                           completion(newStatus == .authorized || newStatus == .limited)
                       }
                   }
               } else {
                   completion(false)
               }
           } else {
               // Fallback on earlier versions
           }
       }
    
}
