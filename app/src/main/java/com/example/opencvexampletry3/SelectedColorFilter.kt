package com.example.opencvexampletry3

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
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


    fun filter_rgb_image(rgb_image: Mat): Mat{

        val imageHSV = Mat(rgb_image.size(), CvType.CV_64F)
        Imgproc.cvtColor(rgb_image, imageHSV, Imgproc.COLOR_RGB2HSV_FULL)

        val hue_img = Mat(rgb_image.size(), CvType.CV_64F)
        Core.extractChannel(imageHSV, hue_img, 0)

        val hue_heatmap = distanceImageToHeatmap(hue_img, peakValue = huePeak, width=hueWidth)





//            Core.multiply(hue_img, Scalar(255.0), hue_img)
//            hue_img.convertTo(out_image, CvType.CV_8UC3)
        Imgproc.cvtColor(hue_img, hue_img, Imgproc.COLOR_GRAY2RGBA)  // Just  copy channel
//        hue_img.convertTo(hue_img, CvType.CV_32FC4)

        rgb_image.convertTo(imageHSV, CvType.CV_32FC4)
        Core.multiply(imageHSV, hue_img, imageHSV)
        imageHSV.convertTo(imageHSV, CvType.CV_8UC3)

        val result = shadeImageByHeatmap(imageRGBA = rgb_image, heatmap = hue_heatmap, inplace = true)

//            val out_image =

//            val real_out_image = Mat.zeros(mRgba_certain.size(), CvType.CV_8UC3)


//            Core.multiply(mRgba_certain, hue_img, out_image)
//
        println("Here")

        return result
    }


}

fun distanceImageToHeatmap(singleChannelImage: Mat, peakValue: Double, width: Double, inplace: Boolean = false): Mat{

//    val hue_img = Mat(rgb_image.size(), CvType.CV_64F)
//    Core.extractChannel(out_image, hue_img, 0)
    val heatmap_image = if (inplace) singleChannelImage else Mat()
    singleChannelImage.convertTo(heatmap_image, CvType.CV_32F)
//            Core.subtract(hue_img, Scalar(100.0), hue_img)
//            val target_hue = mBlobColorHsv!!.`val`[0]


//            val target_hue = 20.0
//        val target_hue = scalar_convert_color(mBlobColorRgba!!, Imgproc.COLOR_RGB2HSV_FULL).`val`[0]
    Core.absdiff(heatmap_image, Scalar(peakValue), heatmap_image)
    Core.divide(heatmap_image, Scalar(-width), heatmap_image)
    Core.exp(heatmap_image, heatmap_image)
    return heatmap_image
}


/** Convert a heatmap (a single channel float image), to a color heatmap (with 4 channels for RGBA) */
fun heatmapToColorHeatmap(heatmap_image: Mat, inplace: Boolean = false): Mat{
    val colourHeatmap = if (inplace) heatmap_image else Mat()
    Imgproc.cvtColor(heatmap_image, colourHeatmap, Imgproc.COLOR_GRAY2RGBA)  // Just  copy channel
    heatmap_image.convertTo(colourHeatmap, CvType.CV_32F)
    return colourHeatmap
}

/** Multiply the RGBA image by the given heatmap */
fun shadeImageByHeatmap(
    imageRGBA: Mat,
    heatmap: Mat,
    inplace: Boolean=false
): Mat{
    val out_image = if (inplace) imageRGBA else Mat()
    imageRGBA.convertTo(out_image, CvType.CV_32FC4)
    Core.multiply(out_image, heatmapToColorHeatmap(heatmap, inplace=inplace), out_image)
    out_image.convertTo(out_image, CvType.CV_8UC3)
    return out_image
}
