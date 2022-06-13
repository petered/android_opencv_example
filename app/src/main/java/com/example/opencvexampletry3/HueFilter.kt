package com.example.opencvexampletry3

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc


class HueFilter(
    val huePeak: Double,
    val hueWidth: Double = 10.0,
    ) {

    companion object {
        fun fromPatch(patchRgba: Mat): HueFilter{
            val touchedRegionHsv = Mat()
            Imgproc.cvtColor(patchRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL)
            val patchHue = Mat()
            val patchSat = Mat()
            Core.extractChannel(touchedRegionHsv, patchHue, 0)
            Core.extractChannel(touchedRegionHsv, patchSat, 1)
            return HueFilter(
                huePeak = Core.mean(patchHue).`val`[0],
            )
        }
    }

    /** Filters the RGB image, returning another image where the selected color is highlighted */
    fun filter_rgb_image(imageRGB: Mat): Mat{
        val imageHSV = outImageAlloc
        Imgproc.cvtColor(imageRGB, imageHSV, Imgproc.COLOR_RGB2HSV_FULL)
        Core.extractChannel(imageHSV, heatMapAlloc, 0)
        val hue_heatmap = distanceImageToHeatmap(heatMapAlloc!!, peakValue = huePeak, width=hueWidth, inplace = true)
        val result = shadeImageByHeatmap(imageRGBA = imageRGB, heatmap = hue_heatmap,
            result_alloc = outImageAlloc, heatmapColorAlloc = heatmapColorAlloc)
        return result
    }

    // Below is code that we need
    private var outImageAlloc: Mat? = null
    private var heatMapAlloc: Mat? = null
    private var heatmapColorAlloc: Mat? = null
    init {
        println("=== Allocation: $outImageAlloc")
        outImageAlloc = Mat()
        heatMapAlloc = Mat()
        heatmapColorAlloc = Mat()
    }

    /* Unfortunately, garbage collector does not work on OpenCV Mats, so we need to be sure to
    manually release the memory to avoid leaks. */
    fun release(){
        outImageAlloc?.release()
        heatMapAlloc?.release()
        heatmapColorAlloc?.release()
    }
}


fun distanceImageToHeatmap(singleChannelImage: Mat, peakValue: Double, width: Double, inplace: Boolean = false): Mat{
    val heatmap_image = if (inplace) singleChannelImage else Mat()
    singleChannelImage.convertTo(heatmap_image, CvType.CV_32F)
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
