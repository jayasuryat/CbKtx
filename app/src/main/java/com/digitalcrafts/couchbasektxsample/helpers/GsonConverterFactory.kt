package com.digitalcrafts.couchbasektxsample.helpers

import com.digitalcrafts.couchbasektx.models.CbModel
import com.digitalcrafts.couchbasektx.serialzation.CbConverterFactory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class GsonConverterFactory : CbConverterFactory {

    private val gson: Gson by lazy { Gson() }

    override fun toMap(model: CbModel): Map<String, Any> {
        val modelJson = gson.toJson(model)
        val hashMapType: Type = object : TypeToken<Map<String, Any>>() {}.type
        return gson.fromJson(modelJson, hashMapType)
    }

    override fun <T> toModel(cls: Class<T>, map: Map<String, Any>): T =
        gson.fromJson(gson.toJson(map), cls)
}