# Android OpenCV example in Kotlin

This sample android app shows a simple interactive image filter with OpenCV.  

You start the app, click on the image, and the colour that you clicked on will be highlighted.

It is modified from Elvis Chidera's [tutorial]([url](https://medium.com/android-news/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c)) on setting up OpenCV on Android.


## Setup

TODO: Verify that this works

1) Clone this repo.
2) Download the latest OpenCV for Android release from [this page]([url](https://opencv.org/releases/)), unpack it somehwhere.
3) Open `/path/to/this/repo/ repo in Android studio
4) Import OpenCV into your project with `File -> New -> Import Module -> (select /path/to/opencv_folder/sdk)`, naming it, for example `opencv455`
5) Add OpenCV as an app dependency with `File -> Project Structure -> app -> Dependencies -> "+" -> `opencv455`
6) Connect your phone to computer, with USB-debugging enabled. 
7) Build and run app - it should pop up on your phone
8) Probably, no image will appear.  On your android phone, find "Permissions Manager -> Camera -> (Add current app).
9) Now camera feed should come in.  Click the screen and see filtering work.
