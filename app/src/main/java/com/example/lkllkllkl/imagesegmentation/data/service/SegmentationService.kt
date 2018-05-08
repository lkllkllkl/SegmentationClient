package com.example.lkllkllkl.imagesegmentation.data.service

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part


interface SegmentationService {
    @Multipart
    @POST("/segmentation")
    fun vocSegmentation(@Part imgPart: MultipartBody.Part): Call<Map<String, String>>

    @Multipart
    @POST("/cloth_segmentation")
    fun clothSegmentation(@Part imgPart: MultipartBody.Part): Call<Map<String, String>>

}