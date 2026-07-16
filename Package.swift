// swift-tools-version:5.9
import PackageDescription

// Swift Package Manager distribution for SmartScanSDK (iOS).
//
// The `url` and `checksum` below are managed automatically by .github/workflows/ios-release.yml:
// each release builds SmartScanSDK.xcframework, zips it, computes the checksum, rewrites this
// file, commits it, and tags the commit — so the tag a consumer resolves always points at a
// Package.swift whose checksum matches the uploaded .zip.
//
// Consume it with:
//   .package(url: "https://github.com/alertecoronahochei/smartid-scango-sdk", from: "0.1.0")

let package = Package(
    name: "SmartScanSDK",
    platforms: [
        .iOS(.v15),
    ],
    products: [
        .library(name: "SmartScanSDK", targets: ["SmartScanSDK"]),
    ],
    targets: [
        .binaryTarget(
            name: "SmartScanSDK",
            // BINARY_TARGET:URL
            url: "https://github.com/alertecoronahochei/smartid-scango-sdk/releases/download/v0.0.0/SmartScanSDK.xcframework.zip",
            // BINARY_TARGET:CHECKSUM
            checksum: "0000000000000000000000000000000000000000000000000000000000000000"
        ),
    ]
)
