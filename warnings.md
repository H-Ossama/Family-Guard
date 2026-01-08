> Task :parent-controller:stripDebugDebugSymbols
Unable to strip the following libraries, packaging them as they are: libimage_processing_util_jni.so. Run with --info option to learn more.

> Task :parent-controller:processDebugResources

> Task :common:compileDebugKotlin
w: file:///C:/Dev/Parental%20Guard/ParentalControl/common/src/main/java/com/parentalguard/common/utils/CategoryMapper.kt:104:26 Parameter 'packageName' is never used
w: file:///C:/Dev/Parental%20Guard/ParentalControl/common/src/main/java/com/parentalguard/common/utils/CategoryMapper.kt:104:47 Parameter 'category' is never used
w: file:///C:/Dev/Parental%20Guard/ParentalControl/common/src/main/java/com/parentalguard/common/utils/DiscoveryUtils.kt:26:32 'resolveService(NsdServiceInfo!, NsdManager.ResolveListener!): Unit' is deprecated. Deprecated in Java

> Task :common:compileDebugJavaWithJavac NO-SOURCE
> Task :common:bundleLibCompileToJarDebug UP-TO-DATE
> Task :common:processDebugJavaRes UP-TO-DATE
> Task :common:bundleLibRuntimeToDirDebug UP-TO-DATE
> Task :parent-controller:mergeLibDexDebug
> Task :parent-controller:mergeExtDexDebug

> Task :parent-controller:compileDebugKotlin
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/network/DeviceClient.kt:183:13 The expression is unused
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/ui/QRScannerScreen.kt:76:26 'setTargetResolution(Size): ImageAnalysis.Builder' is deprecated. Deprecated in Java
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/ui/components/ProgressIndicators.kt:32:5 Parameter 'progressColor' is never used
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/ui/screens/PinLockScreen.kt:93:48 'outlinedTextFieldColors(Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., TextSelectionColors = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ..., Color = ...): TextFieldColors' is deprecated. Renamed to `OutlinedTextFieldDefaults.colors` with additional parameters tocontrol container color based on state.
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/ui/screens/SettingsScreen.kt:166:21 Name shadowed: context
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/ui/screens/SettingsScreen.kt:296:13 Name shadowed: context
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/viewmodel/DiscoveryViewModel.kt:137:47 'getter for host: InetAddress!' is deprecated. Deprecated in Java
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/viewmodel/DiscoveryViewModel.kt:144:38 'getter for host: InetAddress!' is deprecated. Deprecated in Java
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/viewmodel/DiscoveryViewModel.kt:185:61 There is more than one label with such a name in this scope
w: file:///C:/Dev/Parental%20Guard/ParentalControl/parent-controller/src/main/java/com/parentalguard/parent/viewmodel/DiscoveryViewModel.kt:212:61 There is more than one label with such a name in this scope

> Task :parent-controller:compileDebugJavaWithJavac NO-SOURCE
> Task :parent-controller:processDebugJavaRes
> Task :parent-controller:dexBuilderDebug
> Task :parent-controller:mergeProjectDexDebug
> Task :parent-controller:mergeDebugJavaResource
> Task :parent-controller:packageDebug
> Task :parent-controller:createDebugApkListingFileRedirect
> Task :parent-controller:assembleDebug

[Incubating] Problems report is available at: file:///C:/Dev/Parental%20Guard/ParentalControl/build/reports/problems/problems-report.html

Deprecated Gradle features were used in this build, making it incompatible with Gradle 10.0.

You can use '--warning-mode all' to show the individual deprecation warnings and determine if they come from your own scripts or plugins.

For more on this, please refer to https://docs.gradle.org/9.0-milestone-1/userguide/command_line_interface.html#sec:command_line_warnings in the Gradle documentation.

BUILD SUCCESSFUL in 20s
57 actionable tasks: 28 executed, 29 up-to-date

Build Analyzer results available