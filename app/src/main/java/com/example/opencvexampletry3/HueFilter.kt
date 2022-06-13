package com.example.opencvexampletry3

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc


/**
Pixelwise filter applied to an image that filters in pixels that match the selected hue (darkening
pixels with more distant hues).
NOTE: Initializing this class repeatedly will cause a memory leak.  To avoid this, you need to
 call mat.release() on the internally allocated arrays (outImageAlloc etc)
 **/
class HueFilter(
    private val huePeak: Double,
    private val hueWidth: Double = 10.0,
    ) {

    // The below arrays are class members instead of local because opencv is not garbage collected
    // so we need to manually make sure we reuse the same memory with each call to filterRGBImage
    private var outImageAlloc: Mat = Mat()
    private var heatMapAlloc: Mat = Mat()
    private var heatmapColorAlloc: Mat = Mat()

    /** Secondary constructor allowing you to get filter params from an image patch **/
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
    fun filterRGBImage(imageRGB: Mat): Mat {
        val imageHSV = outImageAlloc
        Imgproc.cvtColor(imageRGB, imageHSV, Imgproc.COLOR_RGB2HSV_FULL)
        Core.extractChannel(imageHSV, heatMapAlloc, 0)
        val hueHeatmap = valueImageToClosenessHeatmap(
            heatMapAlloc,
            peakValue = huePeak,
            width = hueWidth,
            inplace = true
        )
        return shadeImageByHeatmap(
            imageRGBA = imageRGB, heatmap = hueHeatmap,
            result_alloc = outImageAlloc, heatmapColorAlloc = heatmapColorAlloc
        )
    }
}


/** Take a single-channel image representing some value, and get the closeness to a target value (max one) **/
fun valueImageToClosenessHeatmap(singleChannelImage: Mat, peakValue: Double, width: Double, inplace: Boolean = false): Mat{
    val heatmapImage = if (inplace) singleChannelImage else Mat()
    singleChannelImage.convertTo(heatmapImage, CvType.CV_32F)
    Core.absdiff(heatmapImage, Scalar(peakValue), heatmapImage)
    Core.divide(heatmapImage, Scalar(-width), heatmapImage)
    Core.exp(heatmapImage, heatmapImage)
    return heatmapImage
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
    val outImage = result_alloc ?: Mat()
    imageRGBA.convertTo(outImage, CvType.CV_32FC4)
    Core.multiply(outImage, heatmapToColorHeatmap(heatmap, colourHeatmapAlloc = heatmapColorAlloc), outImage)
    outImage.convertTo(outImage, CvType.CV_8UC3)
    return outImage
}
