package com.digitalcrafts.couchbasektx.serialzation

import com.digitalcrafts.couchbasektx.models.CbModel

public interface CbConverterFactory {

    public fun toMap(model: CbModel): Map<String, Any>

    public fun <T> toModel(cls: Class<T>, map: Map<String, Any>): T
}