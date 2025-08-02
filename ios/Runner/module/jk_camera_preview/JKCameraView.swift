//
//  JKCameraView.swift
//  Runner
//
//  Created by JunCook on 31/7/25.
//

import SwiftUI

@available(iOS 15.0, *)
struct JKCameraView: View {
    @StateObject private var model = CameraModel()
 
    private static let barHeightFactor = 0.15
    
    var body: some View {
        
            GeometryReader { geometry in
                JKViewfinderView(image:  $model.viewfinderImage )
                    .overlay(alignment: .top) {
                        Color.black
                            .opacity(0.75)
                            .frame(height: geometry.size.height * Self.barHeightFactor)
                    }
                    .overlay(alignment: .bottom) {
                        buttonsView()
                            .frame(height: geometry.size.height * Self.barHeightFactor)
                            .background(.black.opacity(0.75))
                    }
                    .overlay(alignment: .center)  {
                        Color.clear
                            .frame(height: geometry.size.height * (1 - (Self.barHeightFactor * 2)))
                            .accessibilityElement()
                            .accessibilityLabel("View Finder")
                            .accessibilityAddTraits([.isImage])
                    }
                    .background(.black)
            }
            .task {
                await model.camera.start()
                await model.loadPhotos()
                await model.loadThumbnail()
            }
    }
    
    private func buttonsView() -> some View {
        HStack(spacing: 60) {
            
            Spacer()
            
//            NavigationLink {
//                PhotoCollectionView(photoCollection: model.photoCollection)
//                    .onAppear {
//                        model.camera.isPreviewPaused = true
//                    }
//                    .onDisappear {
//                        model.camera.isPreviewPaused = false
//                    }
//            } label: {
//                Label {
//                    Text("Gallery")
//                } icon: {
//                  //  ThumbnailView(image: model.thumbnailImage)
//                }
//            }
            
            Button {
                model.camera.takePhoto()
            } label: {
                Label {
                    Text("Take Photo")
                } icon: {
                    ZStack {
                        Circle()
                            .strokeBorder(.white, lineWidth: 3)
                            .frame(width: 62, height: 62)
                        Circle()
                            .fill(.white)
                            .frame(width: 50, height: 50)
                    }
                }
            }
            
            Button {
                model.camera.switchCaptureDevice()
            } label: {
                Label("Switch Camera", systemImage: "arrow.triangle.2.circlepath")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.white)
            }
            
            Spacer()
        
        }
        .buttonStyle(.plain)
        .labelStyle(.iconOnly)
        .padding()
    }
    
}
