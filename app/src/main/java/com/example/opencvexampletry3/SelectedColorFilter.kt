package com.example.opencvexampletry3

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc

val i = 10

class HueValueFilter(
    val huePeak: Double,
    val satPeak: Double,
    val hueWidth: Double = 10.0,
    val satWidth: Double = 30.0,
    ) {

//    constructor (patch: Mat): this(peakHue=patch.get(0, 0)[0], 3.0){}

    companion object {
        operator fun invoke(patchRgba: Mat): HueValueFilter{
            val touchedRegionHsv = Mat()
            Imgproc.cvtColor(patchRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)
            val patchHue = Mat()
            val patchSat = Mat()
            Core.extractChannel(touchedRegionHsv, patchHue, 0)
            Core.extractChannel(touchedRegionHsv, patchSat, 1)
            return HueValueFilter(
                huePeak = Core.mean(patchHue).`val`[0],
                satPeak = Core.mean(patchSat).`val`[1]
            )
//            Core.mean(patchHue).`val`[0]
//
//            // Calculate average color of touched region
////        println("N Elements ${touchedRegionHsv}")
//            val mBlobColorHsv = Core.sumElems(touchedRegionHsv)
//            println("Blob Color: $mBlobColorHsv")
//            val pointCount = (patchRgba.width() * patchRgba.height()).toDouble()
//            println("Pointcount Value: $pointCount")
//            for (i in mBlobColorHsv.`val`.indices) mBlobColorHsv.`val`[i] /= pointCount
        }
    }


    fun filter_rgb_image(image: Mat): Mat{



    }


}