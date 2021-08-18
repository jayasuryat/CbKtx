package com.digitalcrafts.couchbasektxsample.helpers

import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektxsample.MyApplication

object DatabaseManager {

    val cbHelper: CbHelper by lazy {
        CbHelper.Builder(
            context = MyApplication.application.applicationContext,
            dataBaseName = "myDb",
            converterFactory = GsonConverterFactory()
        ).build()
    }
}