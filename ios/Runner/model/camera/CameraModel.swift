//
//  CameraModel.swift
//  Runner
//
//  Created by JunCook on 31/7/25.
//

import AVFoundation
import SwiftUI
import os.log

final class CameraModel: ObservableObject {
    let camera = Camera()
   // let photoCollection = PhotoCollection(smartAlbum: .smartAlbumUserLibrary)
    
    @Published var viewfinderImage: Image?
    @Published var thumbnailImage: Image?
    
    var isPhotosLoaded = false
    
    init() {
        Task {
            await handleCameraPreviews()
        }
        
//        Task {
//            await handleCameraPhotos()
//        }
    }
    
    func handleCameraPreviews() async {
        let imageStream = camera.previewStream
            .map { $0.image }

        for await image in imageStream {
            Task { @MainActor in
                viewfinderImage = image
            }
        }
    }
    
    func handleCameraPhotos() async {
        let unpackedPhotoStream = camera.photoStream
            .compactMap { self.unpackPhoto($0) }
        
        for await photoData in unpackedPhotoStream {
            Task { @MainActor in
                thumbnailImage = photoData.thumbnailImage
            }
            savePhoto(imageData: photoData.imageData)
        }
    }
    
    private func unpackPhoto(_ photo: AVCapturePhoto) -> PhotoData? {
        guard let imageData = photo.fileDataRepresentation() else { return nil }

        guard let previewCGImage = photo.previewCGImageRepresentation(),
           let metadataOrientation = photo.metadata[String(kCGImagePropertyOrientation)] as? UInt32,
              let cgImageOrientation = CGImagePropertyOrientation(rawValue: metadataOrientation) else { return nil }
        let imageOrientation = Image.Orientation(cgImageOrientation)
        let thumbnailImage = Image(decorative: previewCGImage, scale: 1, orientation: imageOrientation)
        
        let photoDimensions = photo.resolvedSettings.photoDimensions
        let imageSize = (width: Int(photoDimensions.width), height: Int(photoDimensions.height))
        let previewDimensions = photo.resolvedSettings.previewDimensions
        let thumbnailSize = (width: Int(previewDimensions.width), height: Int(previewDimensions.height))
        
        return PhotoData(thumbnailImage: thumbnailImage, thumbnailSize: thumbnailSize, imageData: imageData, imageSize: imageSize)
    }
    
    func savePhoto(imageData: Data) {
//        Task {
//            do {
//                try await photoCollection.addImage(imageData)
//                LoggerCompat.debug("Added image data to photo collection.")
//            } catch let error {
//                LoggerCompat.error("Failed to add image to photo collection: \(error.localizedDescription)")
//            }
//        }
    }
    
    func loadPhotos() async {
//        guard !isPhotosLoaded else { return }
//        
//        let authorized = await PhotoLibrary.checkAuthorization()
//        guard authorized else {
//            LoggerCompat.error("Photo library access was not authorized.")
//            return
//        }
        
//        Task {
//            do {
//                try await self.photoCollection.load()
//                await self.loadThumbnail()
//            } catch let error {
//                LoggerCompat.error("Failed to load photo collection: \(error.localizedDescription)")
//            }
//            self.isPhotosLoaded = true
//        }
    }
    
    func loadThumbnail() async {
//        guard let asset = photoCollection.photoAssets.first  else { return }
//        await photoCollection.cache.requestImage(for: asset, targetSize: CGSize(width: 256, height: 256))  { result in
//            if let result = result {
//                Task { @MainActor in
//                    self.thumbnailImage = result.image
//                }
//            }
//        }
    }
}

fileprivate struct PhotoData {
    var thumbnailImage: Image
    var thumbnailSize: (width: Int, height: Int)
    var imageData: Data
    var imageSize: (width: Int, height: Int)
}

fileprivate extension CIImage {
    var image: Image? {
        let ciContext = CIContext()
        guard let cgImage = ciContext.createCGImage(self, from: self.extent) else { return nil }
        return Image(decorative: cgImage, scale: 1, orientation: .up)
    }
}

fileprivate extension Image.Orientation {

    init(_ cgImageOrientation: CGImagePropertyOrientation) {
        switch cgImageOrientation {
        case .up: self = .up
        case .upMirrored: self = .upMirrored
        case .down: self = .down
        case .downMirrored: self = .downMirrored
        case .left: self = .left
        case .leftMirrored: self = .leftMirrored
        case .right: self = .right
        case .rightMirrored: self = .rightMirrored
        }
    }
}

fileprivate struct LoggerCompat {
    static func debug(_ message: String) {
        if #available(iOS 14.0, *) {
            let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "CameraModel")
            logger.debug("\(message, privacy: .public)")
        } else {
            print("DEBUG: \(message)")
        }
    }

    static func error(_ message: String) {
        if #available(iOS 14.0, *) {
            let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "CameraModel")
            logger.error("\(message, privacy: .public)")
        } else {
            print("ERROR: \(message)")
        }
    }
}
