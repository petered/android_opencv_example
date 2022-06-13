# Android OpenCV example in Kotlin

This sample android app shows a simple interactive image filter with OpenCV.  It displays image from your phone's camera.  You start the app, click on the image, and the colour that you clicked on will be highlighted.


Screenshots: 
![Screenshot_20220613-155732_OpenCVExampleTry3](https://user-images.githubusercontent.com/1148799/173461136-45405b69-e731-4243-858d-230bdbc802ed.jpg)
![Screenshot_20220613-155740_OpenCVExampleTry3](https://user-images.githubusercontent.com/1148799/173461234-b9b9d6e6-77c6-429d-9491-a619d8216ff3.jpg)
![Screenshot_20220613-155746_OpenCVExampleTry3](https://user-images.githubusercontent.com/1148799/173461239-083408cb-0b1a-4728-9150-36bdd6563177.jpg)

It is modified from Elvis Chidera's [tutorial](https://medium.com/android-news/a-beginners-guide-to-setting-up-opencv-android-library-on-android-studio-19794e220f3c) on setting up OpenCV on Android.


## Setup

TODO: Verify that this works

1) Clone this repo: `git clone git@github.com:petered/android_opencv_example.git`
2) Download the latest OpenCV for Android release from [this page]([url](https://opencv.org/releases/)), unpack it somehwhere.
3) Open `/path/to/android_opencv_example/` repo in Android studio
4) Import OpenCV into your project with `File -> New -> Import Module -> (select /path/to/opencv_folder/sdk)`, naming it, for example `opencv455`
5) Add OpenCV as an app dependency with `File -> Project Structure -> app -> Dependencies -> "+" -> `opencv455`
6) Connect your phone to computer, with USB-debugging enabled. 
7) Build and run app - it should pop up on your phone
8) Probably, no image will appear.  That's because you need to add camera permissions (this app is not set up to ask for them).  On your android phone, find "Permissions Manager -> Camera -> (Add current app).
9) Now camera feed should come in.  Click the screen and see filtering work.


## Notes

There is a memory leak - the more times you click the screen (and change the filtering), the more memory gets used up. This appears to be a shortcoming of OpenCV on android - we need to to manual memory management.  See [StackOverflow post](https://stackoverflow.com/questions/72580005/opencv-memory-leak-on-android-garbage-collection-not-working).  It's solveable using `Mat.release()` but that caused another bug that I have not bothered to fix yet.
