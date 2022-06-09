package com.example.opencvexampletry3

import android.app.Activity
import android.view.View.OnTouchListener
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2
import com.example.opencvexampletry3.ColorBlobDetector
import org.opencv.android.CameraBridgeViewBase
import org.opencv.android.BaseLoaderCallback
import org.opencv.android.LoaderCallbackInterface
import com.example.opencvexampletry3.MainActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import com.example.opencvexampletry3.R
import org.opencv.android.OpenCVLoader
import org.opencv.imgproc.Imgproc
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame

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
        mOpenCvCameraView!!.setCvCameraViewListener(this)
    }

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
        val touchedRegionHsv = Mat()
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)

        // Calculate average color of touched region
//        println("N Elements ${touchedRegionHsv}")
        val mBlobColorHsv = Core.sumElems(touchedRegionHsv)
        println("Blob Color: $mBlobColorHsv")
        val pointCount = (touchedRect.width * touchedRect.height).toDouble()
        println("Pointcount Value: $pointCount")
        for (i in mBlobColorHsv.`val`.indices) mBlobColorHsv.`val`[i] /= pointCount
        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv)
        Log.i(
            TAG,
            "Touched rgba color: (" + mBlobColorRgba!!.`val`[0] + ", " + mBlobColorRgba!!.`val`[1] +
                    ", " + mBlobColorRgba!!.`val`[2] + ", " + mBlobColorRgba!!.`val`[3] + ")"
        )
        mDetector!!.setHsvColor(mBlobColorHsv)
        Imgproc.resize(
            mDetector!!.spectrum,
            mSpectrum,
            SPECTRUM_SIZE,
            0.0,
            0.0,
            Imgproc.INTER_LINEAR_EXACT
        )
        mIsColorSelected = true
        touchedRegionRgba.release()
        touchedRegionHsv.release()
        return false // don't need subsequent touch events
    }

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        if (mIsColorSelected) {
            val mRgba_certain = mRgba!!
            val norm = Math.pow(Math.pow(mBlobColorRgba!!.`val`[0], 2.0) +
                    Math.pow(mBlobColorRgba!!.`val`[1], 2.0) +
                    Math.pow(mBlobColorRgba!!.`val`[2], 2.0), 0.5)
            val filter = DoubleArray(3){mBlobColorRgba!!.`val`[it]/norm}
//            val filter = DoubleArray(3){if (it==2) 1.0 else 0.0}
//            Core.transform()
            println("Setting filter from ${mBlobColorRgba!!.`val`.contentToString()} to ${filter.contentToString()} - norm was $norm")

//            val color_transform = Mat(4, 4, CvType.CV_64F, Scalar(doubleArrayOf(1.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0)))
//            val outputImage = Mat()
//            val gray_image = Mat(mRgba_certain.size(), CvType.CV_64FC4)
            val out_image = Mat(mRgba_certain.size(), CvType.CV_64F)
            Imgproc.cvtColor(mRgba_certain, out_image, Imgproc.COLOR_RGB2HSV_FULL)



            val gray_image = Mat(mRgba_certain.size(), CvType.CV_64FC4)
            val color_transform = Mat(4, 4, CvType.CV_64F, Scalar(0.0))
//            val color_transform = Mat.d
            color_transform.put(0, 0, filter[0])
            color_transform.put(1, 1, filter[1])
            color_transform.put(2, 2, filter[2])
            color_transform.put(3, 3, 1.0)


            Imgproc.cvtColor(gray_image, gray_image, Imgproc.COLOR_RGB2GRAY)
            Imgproc.cvtColor(gray_image, gray_image, Imgproc.COLOR_GRAY2RGB)

//            Core.multiply(mRgba_certain, gray_image, mRgba_certain)


//            Core.multiply(mRgba_certain, filter, mRgba_certain)
//            mRgba_certain.let{}
//            Imgproc.color
//            Imgproc.filter2D(mRgba, mRgba, 3, mBlobColorRgba.m)
//            Imgproc.filter2D(mRgba, mRgba, 1, mBlobColorRgba) //            for (i in 0 until mRgba_certain.height()){
//                for (j in 0 until mRgba_certain.width()){
//                    for (c in 0 until 3){
//                        mRgba!!.get(i, j)[c] *= filter[c]
//                    }
//                }
//            }

//            val contours = mDetector!!.process(mRgba)
////            val contours = mDetector!!.contours
//
//
//
//            Log.e(TAG, "Contours count: " + contours.size)
//            Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR)
//            val colorLabel = mRgba!!.submat(4, 68, 4, 68)
//            colorLabel.setTo(mBlobColorRgba)
            val spectrumLabel = mRgba!!.submat(4, 4 + mSpectrum!!.rows(), 70, 70 + mSpectrum!!.cols())
            mSpectrum!!.copyTo(spectrumLabel)
            return gray_image
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