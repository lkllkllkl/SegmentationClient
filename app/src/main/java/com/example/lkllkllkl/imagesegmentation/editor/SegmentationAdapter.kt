package com.example.lkllkllkl.imagesegmentation.editor

import android.support.annotation.LayoutRes
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.lkllkllkl.imagesegmentation.R
import com.example.lkllkllkl.imagesegmentation.data.SegmentationImageEntity
import com.example.lkllkllkl.imagesegmentation.utils.GlideApp

class SegmentationAdapter(
        @LayoutRes layoutRes: Int,
        data: List<SegmentationImageEntity>
) : BaseQuickAdapter<SegmentationImageEntity, BaseViewHolder>(layoutRes, data) {
    override fun convert(helper: BaseViewHolder, item: SegmentationImageEntity) {
        helper.setText(R.id.editor_tv_segmentation, item.className)
        val imageView = helper.getView<ImageView>(R.id.editor_iv_segmentation)
        GlideApp.with(imageView)
                .load(item.url)
                .placeholder(R.drawable.ic_place_holder_gray_24dp)
                .error(R.drawable.ic_error_gray_24dp)
                .centerInside()
                .into(imageView)
    }
}