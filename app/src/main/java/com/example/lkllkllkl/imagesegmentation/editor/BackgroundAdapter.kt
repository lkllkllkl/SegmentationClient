package com.example.lkllkllkl.imagesegmentation.editor

import android.support.annotation.LayoutRes
import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.example.lkllkllkl.imagesegmentation.R
import com.example.lkllkllkl.imagesegmentation.data.BackgroundEntity
import com.example.lkllkllkl.imagesegmentation.utils.GlideApp

class BackgroundAdapter(@LayoutRes layoutResId: Int, data: MutableList<BackgroundEntity>?
) : BaseQuickAdapter<BackgroundEntity, BaseViewHolder>(layoutResId, data) {
    override fun convert(helper: BaseViewHolder, item: BackgroundEntity) {
        val imageView = helper.getView<ImageView>(R.id.editor_iv_background)
        GlideApp.with(imageView)
                .load(item.res)
                .placeholder(R.drawable.ic_place_holder_gray_24dp)
                .error(R.drawable.ic_error_gray_24dp)
                .centerInside()
                .into(imageView)
    }
}