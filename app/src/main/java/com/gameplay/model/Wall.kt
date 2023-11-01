package com.gameplay.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Wall(
    val id: Int? = -1,
    val name: String? = null,
    val image_url: String? = null,
    val thumb_url: String? = null,
    val timestamp: String? = null,
    val download_count: Int? = null,
    val is_delete: Boolean? = false,
    val type: String? = null,
): Parcelable
