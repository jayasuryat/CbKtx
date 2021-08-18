package com.digitalcrafts.couchbasektx.models

public abstract class CbModel {

    @Suppress("PropertyName")
    public var _id: String? = null

    public var type: String? = null
}