package com.example.basecomposemvvm.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BaseResponse<T>(
    @Json(name = "count") val count: Int? = null,
    @Json(name = "results") val data: T?
)