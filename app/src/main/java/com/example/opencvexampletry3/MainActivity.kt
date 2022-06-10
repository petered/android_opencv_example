package com.example.opencvexampletry3

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import org.bytedeco.librealsense.global.RealSense.camera
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import org.opencv.android.LoaderCallbackInterface
import org.opencv.android.OpenCVLoader
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import android.hardware.Camera


//import org.jetbrains.kotlinx.multik.api.math.Math as mkmath

//import org.jetbrains.kotlinx.multik

class MainActivity : Activity(), OnTouchListener, CvCameraViewListener2 {


    private var mIsColorSelected = false
    private var mRgba: Mat? = null
    private var mBlobColorRgba: Scalar? = null
    private var mBlobColorHsv: Scalar? = null
    private var mDetector: ColorBlobDetector? = null
    private var mSpectrum: Mat? = null
    private var SPECTRUM_SIZE: Size? = null
    private var CONTOUR_COLOR: Scalar? = null
    private var mOpenCvCameraView: CameraBridgeViewBase? = null
    private val mLoaderCallback: BaseLoaderCallback = object : BaseLoaderCallback(this) {
        override fun onManagerConnected(status: Int) {
            when (status) {
                SUCCESS -> {
                    Log.i(TAG, "OpenCV loaded successfully")
                    mOpenCvCameraView!!.enableView()
                    mOpenCvCameraView!!.setOnTouchListener(this@MainActivity)
                }
                else -> {
                    super.onManagerConnected(status)
                }
            }
        }
    }

    /** Called when the activity is first created.  */
    public override fun onCreate(savedInstanceState: Bundle?) {
        Log.i(TAG, "called onCreate")
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.color_blob_detection_surface_view)
        mOpenCvCameraView =
            findViewById<View>(R.id.color_blob_detection_activity_surface_view) as CameraBridgeViewBase
        mOpenCvCameraView!!.visibility = SurfaceView.VISIBLE
//        mOpenCvCameraView!!.rotation = 270f
        mOpenCvCameraView!!.setCvCameraViewListener(this)

    }

    private var colourFilter: HueSaturationFilter? = null

//    fun surfaceDestroyed(holder: SurfaceHolder?) {
//        camera.stopPreview()
//        camera.release()
//        camera = null
//        previewing = false
//    }

    public override fun onPause() {
        super.onPause()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    public override fun onResume() {
        super.onResume()
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization")
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!")
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS)
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        if (mOpenCvCameraView != null) mOpenCvCameraView!!.disableView()
    }

    override fun onCameraViewStarted(width: Int, height: Int) {
        mRgba = Mat(height, width, CvType.CV_8UC4)
        mDetector = ColorBlobDetector()
        mSpectrum = Mat()
        mBlobColorRgba = Scalar(255.0)
        mBlobColorRgba = Scalar(255.0)
        mBlobColorHsv = Scalar(255.0)
        SPECTRUM_SIZE = Size(200.0, 64.0)
        CONTOUR_COLOR = Scalar(255.0, 0.0, 0.0, 255.0)
    }

    override fun onCameraViewStopped() {
        mRgba!!.release()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val cols = mRgba!!.cols()
        val rows = mRgba!!.rows()
        val xOffset = (mOpenCvCameraView!!.width - cols) / 2
        val yOffset = (mOpenCvCameraView!!.height - rows) / 2
        val x = event.x.toInt() - xOffset
        val y = event.y.toInt() - yOffset
        Log.i(TAG, "Touch image coordinates: ($x, $y)")
        if (x < 0 || y < 0 || x > cols || y > rows) return false
        val touchedRect = Rect()
        touchedRect.x = if (x > 4) x - 4 else 0
        touchedRect.y = if (y > 4) y - 4 else 0

        println("Touchloc = ${touchedRect.x}, ${touchedRect.y}")
        touchedRect.width = if (x + 4 < cols) x + 4 - touchedRect.x else cols - touchedRect.x
        touchedRect.height = if (y + 4 < rows) y + 4 - touchedRect.y else rows - touchedRect.y
        val touchedRegionRgba = mRgba!!.submat(touchedRect)


        colourFilter = HueSaturationFilter.fromPatch(touchedRegionRgba)
        return false
    }

//
//
//        val touchedRegionHsv = Mat()
//        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)
//
//        // Calculate average color of touched region
////        println("N Elements ${touchedRegionHsv}")
//        val mBlobColorHsv = Core.sumElems(touchedRegionHsv)
//        println("Blob Color: $mBlobColorHsv")
//        val pointCount = (touchedRect.width * touchedRect.height).toDouble()
//        println("Pointcount Value: $pointCount")
//        for (i in mBlobColorHsv.`val`.indices) mBlobColorHsv.`val`[i] /= pointCount
//        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv)
//        Log.i(
//            TAG,
//            "Touched rgba color: (" + mBlobColorRgba!!.`val`[0] + ", " + mBlobColorRgba!!.`val`[1] +
//                    ", " + mBlobColorRgba!!.`val`[2] + ", " + mBlobColorRgba!!.`val`[3] + ")"
//        )
//        mDetector!!.setHsvColor(mBlobColorHsv)
//        Imgproc.resize(
//            mDetector!!.spectrum,
//            mSpectrum,
//            SPECTRUM_SIZE,
//            0.0,
//            0.0,
//            Imgproc.INTER_LINEAR_EXACT
//        )
//        mIsColorSelected = true
//        touchedRegionRgba.release()
//        touchedRegionHsv.release()
//        return false // don't need subsequent touch events
//    }

//    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
//        val rgbaMat: Mat = inputFrame.rgba()!!
//        val lets_have_a_memory_leak = true
//        if (lets_have_a_memory_leak) {
//            val unused_leaker = Mat.zeros(rgbaMat.size(), rgbaMat.type())
//        }
//        return rgbaMat
//    }


//    var out_image: Mat? = null
//    var hue_img: Mat? = null

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        if (colourFilter != null) {
            return colourFilter!!.filter_rgb_image(mRgba!!)
//
//            // Get the filter params
//            val mRgba_certain = mRgba!!
//            val norm = Math.pow(Math.pow(mBlobColorRgba!!.`val`[0], 2.0) +
//                    Math.pow(mBlobColorRgba!!.`val`[1], 2.0) +
//                    Math.pow(mBlobColorRgba!!.`val`[2], 2.0), 0.5)
//            val filter = DoubleArray(3){mBlobColorRgba!!.`val`[it]/norm}
//            println("Setting filter from ${mBlobColorRgba!!.`val`.contentToString()} to ${filter.contentToString()} - norm was $norm")
//
//            if (out_image == null)
//                out_image = Mat(mRgba_certain.size(), CvType.CV_64F)
////            val out_image = Mat(mRgba_certain.size(), CvType.CV_64F)
////            val out_image = Mat()
//            Imgproc.cvtColor(mRgba_certain, out_image, Imgproc.COLOR_RGB2HSV_FULL)
//
//            // Extract the hue image, make a heatmap
//            if (hue_img == null){hue_img = Mat()}
//            Core.extractChannel(out_image, hue_img, 0)
//
//            hue_img!!.convertTo(hue_img, CvType.CV_32F)
//            val target_hue = scalar_convert_color(mBlobColorRgba!!, Imgproc.COLOR_RGB2HSV_FULL).`val`[0]
//            println("Target Hue: $target_hue")
//            Core.absdiff(hue_img, Scalar(target_hue), hue_img)
//            Core.divide(hue_img, Scalar(-10.0), hue_img)
//            Core.exp(hue_img, hue_img)
//
//            // Combine heatmap with RGB to make output image
//            Imgproc.cvtColor(hue_img, hue_img, Imgproc.COLOR_GRAY2RGBA)  // Just  copy channel
//            hue_img!!.convertTo(hue_img, CvType.CV_32FC4)
//            mRgba_certain.convertTo(out_image, CvType.CV_32FC4)
//            Core.multiply(out_image, hue_img, out_image)
//            out_image!!.convertTo(out_image, CvType.CV_8UC3)
//            println("Here")
//            val spectrumLabel = mRgba!!.submat(4, 4 + mSpectrum!!.rows(), 70, 70 + mSpectrum!!.cols())
//            mSpectrum!!.copyTo(spectrumLabel)
//            return out_image!!
        }
        return mRgba!!
    }

    private fun converScalarHsv2Rgba(hsvColor: Scalar?): Scalar {
        val pointMatRgba = Mat()
        val pointMatHsv = Mat(1, 1, CvType.CV_8UC3, hsvColor)
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4)
        return Scalar(pointMatRgba[0, 0])
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    init {
        Log.i(TAG, "Instantiated new " + this.javaClass)
    }
}

fun scalar_convert_color(color: Scalar, code: Int): Scalar{

    val original = Mat(1, 1, CvType.CV_8UC3, color)
    val outmat = Mat()
    Imgproc.cvtColor(original, outmat, code)
    return Scalar(outmat.get(0, 0))

}