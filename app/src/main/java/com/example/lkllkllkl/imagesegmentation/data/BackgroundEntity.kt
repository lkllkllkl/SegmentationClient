package com.example.lkllkllkl.imagesegmentation.data

import android.support.annotation.DrawableRes

data class BackgroundEntity(
        var path: String = "",
        @DrawableRes var res: Int = 0,
        var selected: Boolean = false)