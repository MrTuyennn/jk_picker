//
//  JKViewfinderView.swift
//  Runner
//
//  Created by JunCook on 31/7/25.
//

import SwiftUI

struct JKViewfinderView: View {
    @Binding var image: Image?
    
    var body: some View {
        GeometryReader { geometry in
            if let image = image {
                image
                    .resizable()
                    .scaledToFill()
                    .frame(width: geometry.size.width, height: geometry.size.height)
            }
        }
    }
}
