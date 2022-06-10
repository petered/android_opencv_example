package com.example.opencvexampletry3

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc


class HueSaturationFilter(
    val huePeak: Double,
    val satPeak: Double,
    val hueWidth: Double = 10.0,
    val satWidth: Double = 30.0,
    ) {

    private var outImageAlloc: Mat? = null
    private var heatMapAlloc: Mat? = null
    private var heatmapColorAlloc: Mat? = null
//    constructor (patch: Mat): this(peakHue=patch.get(0, 0)[0], 3.0){}

    init {
        outImageAlloc = Mat()
        heatMapAlloc = Mat()
        heatmapColorAlloc = Mat()
    }

    companion object {
        fun fromPatch(patchRgba: Mat): HueSaturationFilter{
            val touchedRegionHsv = Mat()
            Imgproc.cvtColor(patchRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)
            val patchHue = Mat()
            val patchSat = Mat()
            Core.extractChannel(touchedRegionHsv, patchHue, 0)
            Core.extractChannel(touchedRegionHsv, patchSat, 1)
            return HueSaturationFilter(
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


    fun filter_rgb_image(imageRGB: Mat): Mat{

//        val imageHSV = Mat(imageRGB.size(), CvType.CV_64F)
        val imageHSV = outImageAlloc
        Imgproc.cvtColor(imageRGB, imageHSV, Imgproc.COLOR_RGB2HSV_FULL)

//        if (outImageAlloc==null) {outImageAlloc = Mat()}
//        if (heatMapAlloc==null) {heatMapAlloc = Mat()}
//
        Core.extractChannel(imageHSV, heatMapAlloc, 0)
        val hue_heatmap = distanceImageToHeatmap(heatMapAlloc!!, peakValue = huePeak, width=hueWidth, inplace = true)
        val result = shadeImageByHeatmap(imageRGBA = imageRGB, heatmap = hue_heatmap,
            result_alloc = outImageAlloc, heatmapColorAlloc = heatmapColorAlloc)
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
fun heatmapToColorHeatmap(heatmap_image: Mat, colourHeatmapAlloc: Mat? = null): Mat{
    val colourHeatmap = colourHeatmapAlloc ?: Mat()
    Imgproc.cvtColor(heatmap_image, colourHeatmap, Imgproc.COLOR_GRAY2RGBA)  // Just  copy channel
    colourHeatmap.convertTo(colourHeatmap, CvType.CV_32F)
    return colourHeatmap
}

/** Multiply the RGBA image by the given heatmap */
fun shadeImageByHeatmap(
    imageRGBA: Mat,
    heatmap: Mat,
    heatmapColorAlloc: Mat? = null,
    result_alloc: Mat? = null
): Mat{
    val out_image = result_alloc ?: Mat()
    imageRGBA.convertTo(out_image, CvType.CV_32FC4)
    Core.multiply(out_image, heatmapToColorHeatmap(heatmap, colourHeatmapAlloc = heatmapColorAlloc), out_image)
    out_image.convertTo(out_image, CvType.CV_8UC3)
    return out_image
}
