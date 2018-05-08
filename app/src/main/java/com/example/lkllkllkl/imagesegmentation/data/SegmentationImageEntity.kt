package com.example.lkllkllkl.imagesegmentation.data

import android.os.Parcel
import android.os.Parcelable

data class SegmentationImageEntity(
        /**
         * 图片链接
         */
        var url: String = "",
        /**
         * 图片存储路径
         */
        var path: String = "",
        /**
         * 分割结果类名
         */
        var className: String = "",
        /**
         * 是否已经添加
         */
        var isAdded: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readInt() == 1)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(path)
        parcel.writeString(className)
        parcel.writeByte((if (isAdded) 1 else 0))

    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<SegmentationImageEntity> {
        override fun createFromParcel(parcel: Parcel): SegmentationImageEntity {
            return SegmentationImageEntity(parcel)
        }

        override fun newArray(size: Int): Array<SegmentationImageEntity?> {
            return arrayOfNulls(size)
        }
    }
}