package com.digitalcrafts.couchbasektx.cbHelper.replication

import com.couchbase.lite.Authenticator
import com.couchbase.lite.BasicAuthenticator
import com.couchbase.lite.SessionAuthenticator

public sealed class CbAuthenticator {
    internal abstract fun getAuthenticator(): Authenticator
}

public data class CbBasicAuthentication(
    private val username: String,
    private val password: String,
) : CbAuthenticator() {

    override fun getAuthenticator(): Authenticator =
        BasicAuthenticator(username, password.toCharArray())
}

public data class CbSessionBasedAuthentication(
    private val sessionToken: String
) : CbAuthenticator() {

    override fun getAuthenticator(): Authenticator = SessionAuthenticator(sessionToken)
}
