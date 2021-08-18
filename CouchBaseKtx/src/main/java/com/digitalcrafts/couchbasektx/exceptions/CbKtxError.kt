package com.digitalcrafts.couchbasektx.exceptions

public sealed interface CbKtxError

public sealed class QueryError(message: String) : Error(message), CbKtxError {

    public class ModelNotFoundError private constructor(message: String) : QueryError(message) {

        @PublishedApi
        internal companion object {

            fun forId(documentId: String): ModelNotFoundError =
                ModelNotFoundError("No model found for id : $documentId")
        }
    }
}