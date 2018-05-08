package com.example.lkllkllkl.imagesegmentation.data

import com.example.lkllkllkl.imagesegmentation.data.service.SegmentationService
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Callback
import java.io.File


class SegmentationRepository {

    fun segmentation(imgPath: String, type: Int = VOC_SEGMENTATION, callback: Callback<Map<String, String>>) {
        val service = RetrofitUtils.getService(SegmentationService::class.java)
        val imgPart = getMultipartBody(imgPath)
        val call = when (type) {
            CLOTH_SEGMENTATION -> {
                service.clothSegmentation(imgPart)
            }
            else -> {
                service.vocSegmentation(imgPart)
            }
        }
        call.enqueue(callback)
    }

    private fun getMultipartBody(imgPath: String): MultipartBody.Part {
        val file = File(imgPath)
        val imageBody = RequestBody.create(MediaType.parse("multipart/form-data"), file)
        return MultipartBody
                .Part
                .createFormData(IMAGE_KEY_NAME, file.name, imageBody)

    }

    companion object {
        const val IMAGE_KEY_NAME = "file"
        const val VOC_SEGMENTATION = 0
        const val CLOTH_SEGMENTATION = 1
    }
}