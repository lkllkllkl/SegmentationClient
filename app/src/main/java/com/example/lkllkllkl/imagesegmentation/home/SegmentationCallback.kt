package com.example.lkllkllkl.imagesegmentation.home

import android.content.Intent
import android.widget.Toast
import com.example.lkllkllkl.imagesegmentation.data.RetrofitUtils
import com.example.lkllkllkl.imagesegmentation.data.SegmentationImageEntity
import com.example.lkllkllkl.imagesegmentation.editor.EditorActivity
import com.example.lkllkllkl.imagesegmentation.home.HomeActivity.Companion.EXTRA_SEGMENTATION_IMG
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.ref.WeakReference

class SegmentationCallback(context: HomeActivity) : Callback<Map<String, String>> {

    val weakContext = WeakReference<HomeActivity>(context)


    override fun onFailure(call: Call<Map<String, String>>?, t: Throwable?) {

        weakContext.get()?.let {
            Toast.makeText(it, "segmentation failed.", Toast.LENGTH_SHORT).show()
            it.showLoading(false)
        }
    }

    override fun onResponse(call: Call<Map<String, String>>?, response: Response<Map<String, String>>?) {
        weakContext.get()?.let { it.showLoading(false) }
        val map = response?.body()

        map?.let {
            val entities = ArrayList<SegmentationImageEntity>(it.size)
            for ((k, v) in it) {
                entities.add(SegmentationImageEntity(url = RetrofitUtils.BASE_URL + v,
                        className = k))
            }
            println(entities)
            weakContext.get()?.let {
                val intent = Intent(it, EditorActivity::class.java)
                intent.putParcelableArrayListExtra(EXTRA_SEGMENTATION_IMG, entities)
                weakContext.get()?.startActivity(intent)
            }
        }
    }


}