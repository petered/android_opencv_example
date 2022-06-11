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


class MainActivity : Activity(), OnTouchListener, CvCameraViewListener2 {

    private var mRgba: Mat? = null
    private var mDetector: ColorBlobDetector? = null
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

    private var colourFilter: HueSaturationFilter? = null

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
    }

    override fun onCameraViewStopped() {
        mRgba!!.release()
    }

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        val cols = mRgba!!.cols()
        val rows = mRgba!!.rows()
        val xOffset = (v.width - cols) / 2
        val yOffset = (v.height - rows) / 2
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

    override fun onCameraFrame(inputFrame: CvCameraViewFrame): Mat {
        mRgba = inputFrame.rgba()
        return colourFilter?.filter_rgb_image(mRgba!!) ?: mRgba!!
    }

    companion object {
        private const val TAG = "MainActivity"
    }

    init {
        Log.i(TAG, "Instantiated new " + this.javaClass)
    }
}