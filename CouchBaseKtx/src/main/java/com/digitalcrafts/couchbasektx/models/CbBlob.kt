package com.digitalcrafts.couchbasektx.models

import com.couchbase.lite.Blob

public data class CbBlob internal constructor(
    val attachmentName: String,
    val data: ByteArray,
    val mimeType: String,
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CbBlob
        if (attachmentName != other.attachmentName) return false
        if (mimeType != other.mimeType) return false
        return true
    }

    override fun hashCode(): Int {
        var result = attachmentName.hashCode()
        result = 31 * result + mimeType.hashCode()
        return result
    }

    @PublishedApi
    internal companion object {

        fun from(name: String, blob: Blob): CbBlob {

            val output = ByteArray(blob.length().toInt())
            blob.contentStream?.read(output)
            blob.contentStream?.close()

            return CbBlob(
                attachmentName = name,
                data = output,
                mimeType = blob.contentType
            )
        }
    }
}