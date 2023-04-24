package com.kylecorry.aurora

import com.google.gson.annotations.SerializedName

internal data class RawSpaceWeatherAlert(
    @SerializedName("product_id") val productId: String,
    @SerializedName("issue_datetime") val issueDatetime: String,
    val message: String
)