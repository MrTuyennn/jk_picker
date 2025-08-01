//
//  Camera.swift
//  Runner
//
//  Created by JunCook on 31/7/25.
//
import AVFoundation  // Framework để làm việc với camera và media
import CoreImage     // Framework để xử lý hình ảnh
import UIKit         // Framework UI
import os.log        // Framework để log

class Camera: NSObject {
    // AVCaptureSession: Quản lý session chụp ảnh/quay video
    private let captureSession = AVCaptureSession()
    
    // Flag để kiểm tra session đã được cấu hình chưa
    private var isCaptureSessionConfigured = false
    
    // Input từ camera device
    private var deviceInput: AVCaptureDeviceInput?
    
    // Output để chụp ảnh
    private var photoOutput: AVCapturePhotoOutput?
    
    // Output để lấy video data (preview)
    private var videoOutput: AVCaptureVideoDataOutput?
    
    // Queue riêng để xử lý camera operations (tránh block main thread)
    private var sessionQueue: DispatchQueue!
    
    // Lấy tất cả camera devices có sẵn
    private var allCaptureDevices: [AVCaptureDevice] {
        AVCaptureDevice.DiscoverySession(deviceTypes: [.builtInTrueDepthCamera, .builtInDualCamera, .builtInDualWideCamera, .builtInWideAngleCamera, .builtInDualWideCamera], mediaType: .video, position: .unspecified).devices
    }
    
    // Lọc ra camera trước (selfie)
    private var frontCaptureDevices: [AVCaptureDevice] {
        allCaptureDevices
            .filter { $0.position == .front }
    }
    
    // Lọc ra camera sau (back camera)
    private var backCaptureDevices: [AVCaptureDevice] {
        allCaptureDevices
            .filter { $0.position == .back }
    }
    
    // Danh sách camera có thể sử dụng
    private var captureDevices: [AVCaptureDevice] {
        var devices = [AVCaptureDevice]()
        #if os(macOS) || (os(iOS) && targetEnvironment(macCatalyst))
        devices += allCaptureDevices
        #else
        // Trên iOS, chỉ lấy camera trước và sau
        if let backDevice = backCaptureDevices.first {
            devices += [backDevice]
        }
        if let frontDevice = frontCaptureDevices.first {
            devices += [frontDevice]
        }
        #endif
        return devices
    }
    
    // Lọc ra camera đang hoạt động (không bị disconnect/suspended)
    private var availableCaptureDevices: [AVCaptureDevice] {
        if #available(iOS 14.0, *) {
                return captureDevices
                    .filter { $0.isConnected }      // Camera còn kết nối
                    .filter { !$0.isSuspended }    // Camera không bị suspended
            } else {
                // Trên iOS 13, không có isConnected và isSuspended
                return captureDevices
            }
    }
    
    // Camera device hiện tại đang sử dụng
    private var captureDevice: AVCaptureDevice? {
        didSet {
            guard let captureDevice = captureDevice else { return }
            LoggerCompat.debug("Using capture device: \(captureDevice.localizedName)")
            sessionQueue.async {
                self.updateSessionForCaptureDevice(captureDevice)  // Cập nhật session khi đổi camera
            }
        }
    }
    
    // Kiểm tra camera có đang chạy không
    var isRunning: Bool {
        captureSession.isRunning
    }
    
    // Kiểm tra có đang dùng camera trước không
    var isUsingFrontCaptureDevice: Bool {
        guard let captureDevice = captureDevice else { return false }
        return frontCaptureDevices.contains(captureDevice)
    }
    
    // Kiểm tra có đang dùng camera sau không
    var isUsingBackCaptureDevice: Bool {
        guard let captureDevice = captureDevice else { return false }
        return backCaptureDevices.contains(captureDevice)
    }

    // Callback để thêm ảnh vào photo stream
    private var addToPhotoStream: ((AVCapturePhoto) -> Void)?
    
    // Callback để thêm preview vào preview stream
    private var addToPreviewStream: ((CIImage) -> Void)?
    
    // Flag để pause preview (khi chuyển màn hình)
    var isPreviewPaused = false
    
    // AsyncStream để stream preview images
    lazy var previewStream: AsyncStream<CIImage> = {
        AsyncStream { continuation in
            addToPreviewStream = { ciImage in
                if !self.isPreviewPaused {  // Chỉ stream khi không pause
                    continuation.yield(ciImage)
                }
            }
        }
    }()
    
    // AsyncStream để stream captured photos
    lazy var photoStream: AsyncStream<AVCapturePhoto> = {
        AsyncStream { continuation in
            addToPhotoStream = { photo in
                continuation.yield(photo)
            }
        }
    }()
        
    override init() {
        super.init()
        initialize()  // Khởi tạo camera
    }
    
    // Khởi tạo camera
    private func initialize() {
        // Tạo queue riêng cho camera operations
        sessionQueue = DispatchQueue(label: "session queue")
        
        // Chọn camera đầu tiên có sẵn, nếu không có thì dùng default
        captureDevice = availableCaptureDevices.first ?? AVCaptureDevice.default(for: .video)
        
        // Bắt đầu lắng nghe thay đổi orientation của device
        UIDevice.current.beginGeneratingDeviceOrientationNotifications()
        NotificationCenter.default.addObserver(self, selector: #selector(updateForDeviceOrientation), name: UIDevice.orientationDidChangeNotification, object: nil)
    }
    
    // Cấu hình capture session
    private func configureCaptureSession(completionHandler: (_ success: Bool) -> Void) {
        
        var success = false
        
        // Bắt đầu cấu hình session
        self.captureSession.beginConfiguration()
        
        // Đảm bảo commit configuration khi function kết thúc
        defer {
            self.captureSession.commitConfiguration()
            completionHandler(success)
        }
        
        // Tạo device input từ camera device
        guard
            let captureDevice = captureDevice,
            let deviceInput = try? AVCaptureDeviceInput(device: captureDevice)
        else {
            LoggerCompat.error("Failed to obtain video input.")
            return
        }
        
        // Tạo photo output để chụp ảnh
        let photoOutput = AVCapturePhotoOutput()
                        
        // Set preset cho session (photo quality)
        captureSession.sessionPreset = AVCaptureSession.Preset.photo

        // Tạo video output để lấy preview
        let videoOutput = AVCaptureVideoDataOutput()
        videoOutput.setSampleBufferDelegate(self, queue: DispatchQueue(label: "VideoDataOutputQueue"))
  
        // Kiểm tra và thêm input vào session
        guard captureSession.canAddInput(deviceInput) else {
            LoggerCompat.error("Unable to add device input to capture session.")
            return
        }
        // Kiểm tra và thêm photo output vào session
        guard captureSession.canAddOutput(photoOutput) else {
            LoggerCompat.error("Unable to add photo output to capture session.")
            return
        }
        // Kiểm tra và thêm video output vào session
        guard captureSession.canAddOutput(videoOutput) else {
            LoggerCompat.error("Unable to add video output to capture session.")
            return
        }
        
        // Thêm input và outputs vào session
        captureSession.addInput(deviceInput)
        captureSession.addOutput(photoOutput)
        captureSession.addOutput(videoOutput)
        
        // Lưu references
        self.deviceInput = deviceInput
        self.photoOutput = photoOutput
        self.videoOutput = videoOutput
        
        // Cấu hình photo output
        photoOutput.isHighResolutionCaptureEnabled = true  // Bật chụp độ phân giải cao
        photoOutput.maxPhotoQualityPrioritization = .quality  // Ưu tiên chất lượng
        
        // Cập nhật video output connection
        updateVideoOutputConnection()
        
        // Đánh dấu session đã được cấu hình
        isCaptureSessionConfigured = true
        
        success = true
    }
    
    // Kiểm tra quyền truy cập camera
    private func checkAuthorization() async -> Bool {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            LoggerCompat.debug("Camera access authorized.")
            return true
        case .notDetermined:
            LoggerCompat.debug("Camera access not determined.")
            sessionQueue.suspend()  // Tạm dừng queue
            let status = await AVCaptureDevice.requestAccess(for: .video)  // Yêu cầu quyền
            sessionQueue.resume()   // Tiếp tục queue
            return status
        case .denied:
            LoggerCompat.debug("Camera access denied.")
            return false
        case .restricted:
            LoggerCompat.debug("Camera library access restricted.")
            return false
        @unknown default:
            return false
        }
    }
    
    // Tạo device input từ device
    private func deviceInputFor(device: AVCaptureDevice?) -> AVCaptureDeviceInput? {
        guard let validDevice = device else { return nil }
        do {
            return try AVCaptureDeviceInput(device: validDevice)
        } catch let error {
            LoggerCompat.error("Error getting capture device input: \(error.localizedDescription)")
            return nil
        }
    }
    
    // Cập nhật session khi đổi camera device
    private func updateSessionForCaptureDevice(_ captureDevice: AVCaptureDevice) {
        guard isCaptureSessionConfigured else { return }
        
        captureSession.beginConfiguration()
        defer { captureSession.commitConfiguration() }

        // Xóa tất cả inputs cũ
        for input in captureSession.inputs {
            if let deviceInput = input as? AVCaptureDeviceInput {
                captureSession.removeInput(deviceInput)
            }
        }
        
        // Thêm input mới
        if let deviceInput = deviceInputFor(device: captureDevice) {
            if !captureSession.inputs.contains(deviceInput), captureSession.canAddInput(deviceInput) {
                captureSession.addInput(deviceInput)
            }
        }
        
        // Cập nhật video output connection
        updateVideoOutputConnection()
    }
    
    // Cập nhật video output connection (mirror cho camera trước)
    private func updateVideoOutputConnection() {
        if let videoOutput = videoOutput, let videoOutputConnection = videoOutput.connection(with: .video) {
            if videoOutputConnection.isVideoMirroringSupported {
                videoOutputConnection.isVideoMirrored = isUsingFrontCaptureDevice  // Mirror cho camera trước
            }
        }
    }
    
    // Bắt đầu camera
    func start() async {
        // Kiểm tra quyền truy cập
        let authorized = await checkAuthorization()
        guard authorized else {
            LoggerCompat.error("Camera access was not authorized.")
            return
        }
        
        // Nếu session đã được cấu hình, chỉ start running
        if isCaptureSessionConfigured {
            if !captureSession.isRunning {
                sessionQueue.async { [self] in
                    self.captureSession.startRunning()
                }
            }
            return
        }
        
        // Cấu hình session trước khi start
        sessionQueue.async { [self] in
            self.configureCaptureSession { success in
                guard success else { return }
                self.captureSession.startRunning()
            }
        }
    }
    
    // Dừng camera
    func stop() {
        guard isCaptureSessionConfigured else { return }
        
        if captureSession.isRunning {
            sessionQueue.async {
                self.captureSession.stopRunning()
            }
        }
    }
    
    // Chuyển đổi giữa camera trước và sau
    func switchCaptureDevice() {
        if let captureDevice = captureDevice, let index = availableCaptureDevices.firstIndex(of: captureDevice) {
            let nextIndex = (index + 1) % availableCaptureDevices.count  // Chuyển sang camera tiếp theo
            self.captureDevice = availableCaptureDevices[nextIndex]
        } else {
            self.captureDevice = AVCaptureDevice.default(for: .video)  // Dùng camera default
        }
    }

    // Lấy orientation hiện tại của device
    private var deviceOrientation: UIDeviceOrientation {
        var orientation = UIDevice.current.orientation
        if orientation == UIDeviceOrientation.unknown {
            orientation = UIScreen.main.orientation  // Fallback về screen orientation
        }
        return orientation
    }
    
    // Callback khi device orientation thay đổi
    @objc
    func updateForDeviceOrientation() {
        //TODO: Figure out if we need this for anything.
    }
    
    // Chuyển đổi device orientation thành video orientation
    private func videoOrientationFor(_ deviceOrientation: UIDeviceOrientation) -> AVCaptureVideoOrientation? {
        switch deviceOrientation {
        case .portrait: return AVCaptureVideoOrientation.portrait
        case .portraitUpsideDown: return AVCaptureVideoOrientation.portraitUpsideDown
        case .landscapeLeft: return AVCaptureVideoOrientation.landscapeRight
        case .landscapeRight: return AVCaptureVideoOrientation.landscapeLeft
        default: return nil
        }
    }
    
    // Chụp ảnh
    func takePhoto() {
        guard let photoOutput = self.photoOutput else { return }
        
        sessionQueue.async {
        
            // Tạo photo settings
            var photoSettings = AVCapturePhotoSettings()

            // Ưu tiên HEVC codec nếu có
            if photoOutput.availablePhotoCodecTypes.contains(.hevc) {
                photoSettings = AVCapturePhotoSettings(format: [AVVideoCodecKey: AVVideoCodecType.hevc])
            }
            
            // Cấu hình flash
            let isFlashAvailable = self.deviceInput?.device.isFlashAvailable ?? false
            photoSettings.flashMode = isFlashAvailable ? .auto : .off
            
            // Bật chụp độ phân giải cao
            photoSettings.isHighResolutionPhotoEnabled = true
            
            // Cấu hình preview format
            if let previewPhotoPixelFormatType = photoSettings.availablePreviewPhotoPixelFormatTypes.first {
                photoSettings.previewPhotoFormat = [kCVPixelBufferPixelFormatTypeKey as String: previewPhotoPixelFormatType]
            }
            
            // Ưu tiên chất lượng cân bằng
            photoSettings.photoQualityPrioritization = .balanced
            
            // Cấu hình video orientation
            if let photoOutputVideoConnection = photoOutput.connection(with: .video) {
                if photoOutputVideoConnection.isVideoOrientationSupported,
                    let videoOrientation = self.videoOrientationFor(self.deviceOrientation) {
                    photoOutputVideoConnection.videoOrientation = videoOrientation
                }
            }
            
            // Thực hiện chụp ảnh
            photoOutput.capturePhoto(with: photoSettings, delegate: self)
        }
    }
}

// MARK: - AVCapturePhotoCaptureDelegate
// Delegate để xử lý khi chụp ảnh xong
extension Camera: AVCapturePhotoCaptureDelegate {
    
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        
        if let error = error {
            LoggerCompat.error("Error capturing photo: \(error.localizedDescription)")
            return
        }
        
        // Gửi ảnh vào photo stream
        addToPhotoStream?(photo)
    }
}

// MARK: - AVCaptureVideoDataOutputSampleBufferDelegate
// Delegate để xử lý video preview frames
extension Camera: AVCaptureVideoDataOutputSampleBufferDelegate {
    
    func captureOutput(_ output: AVCaptureOutput, didOutput sampleBuffer: CMSampleBuffer, from connection: AVCaptureConnection) {
        guard let pixelBuffer = sampleBuffer.imageBuffer else { return }
        
        // Cấu hình video orientation cho preview
        if connection.isVideoOrientationSupported,
           let videoOrientation = videoOrientationFor(deviceOrientation) {
            connection.videoOrientation = videoOrientation
        }

        // Gửi preview frame vào preview stream
        addToPreviewStream?(CIImage(cvPixelBuffer: pixelBuffer))
    }
}

// MARK: - UIScreen Extension
// Extension để lấy orientation từ UIScreen
fileprivate extension UIScreen {

    var orientation: UIDeviceOrientation {
        let point = coordinateSpace.convert(CGPoint.zero, to: fixedCoordinateSpace)
        if point == CGPoint.zero {
            return .portrait
        } else if point.x != 0 && point.y != 0 {
            return .portraitUpsideDown
        } else if point.x == 0 && point.y != 0 {
            return .landscapeRight //.landscapeLeft
        } else if point.x != 0 && point.y == 0 {
            return .landscapeLeft //.landscapeRight
        } else {
            return .unknown
        }
    }
}

// MARK: - LoggerCompat
// Utility để log messages
fileprivate struct LoggerCompat {
    static func debug(_ message: String) {
        if #available(iOS 14.0, *) {
            let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "Camera")
            logger.debug("\(message, privacy: .public)")
        } else {
            print("DEBUG: \(message)")
        }
    }

    static func error(_ message: String) {
        if #available(iOS 14.0, *) {
            let logger = Logger(subsystem: "com.apple.swiftplaygroundscontent.capturingphotos", category: "Camera")
            logger.error("\(message, privacy: .public)")
        } else {
            print("ERROR: \(message)")
        }
    }
}
