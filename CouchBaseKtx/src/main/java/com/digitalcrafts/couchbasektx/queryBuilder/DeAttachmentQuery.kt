package com.digitalcrafts.couchbasektx.queryBuilder

import com.digitalcrafts.couchbasektx.cbHelper.CbHelper
import com.digitalcrafts.couchbasektx.models.CbBlob
import com.digitalcrafts.couchbasektx.models.QueryResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

public class DeAttachmentQuery private constructor(
    private val cbHelper: CbHelper,
) {

    public class AttachmentException(message: String) : Exception(message)

    @Suppress("NOTHING_TO_INLINE")
    private inline fun fail(message: String): Nothing = throw AttachmentException(message)

    public suspend fun getAttachment(
        documentId: String,
        attachmentName: String
    ): QueryResult<List<CbBlob>> {

        return withContext(Dispatchers.IO) {

            val result = kotlin.runCatching {

                val document = cbHelper.getDataBase().getDocument(documentId)
                    ?: fail("Document with id $documentId not found")

                var blobs: List<CbBlob>? = document
                    .getBlob(attachmentName)
                    ?.let { listOf(CbBlob.from(name = attachmentName, blob = it)) }

                if (blobs == null) {

                    val attachments = document.getDictionary(KEY_ATTACHMENTS)
                        ?: fail("No attachments dictionary found")

                    blobs = attachments
                        .getBlob(attachmentName)
                        ?.let { listOf(CbBlob.from(name = attachmentName, blob = it)) }

                    if (blobs == null) {

                        blobs = attachments.toMap().keys.mapNotNull { key ->
                            attachments.getBlob(key)
                                ?.let { CbBlob.from(name = key, blob = it) }
                        }.takeIf { it.isNotEmpty() }
                    }
                }

                if (!blobs.isNullOrEmpty()) blobs
                else fail("No attachments found in the document with id $documentId")
            }

            result.exceptionOrNull()
                ?.let { ex -> QueryResult.error(throwable = ex) }
                ?: run { QueryResult.success(result.getOrThrow()) }
        }
    }

    public companion object {

        private const val KEY_ATTACHMENTS: String = "_attachments"

        public fun from(cbHelper: CbHelper): DeAttachmentQuery = DeAttachmentQuery(cbHelper)
    }
}