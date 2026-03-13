package com.example.p2pnodereciever.domain

import androidx.annotation.StringRes
import com.example.p2pnodereciever.R

open class IPFSException(
    message: String? = null,
    cause: Throwable? = null
) : RuntimeException(
    message,
    cause
) {
    @StringRes
    open val errorMessage: Int = R.string.error_message
}

class IPFSTimeoutException(
    message: String? = null,
    cause: Throwable? = null
) : IPFSException(
    message,
    cause
) {
    @StringRes
    override val errorMessage: Int = R.string.error_message_timeout
}

class IPFSCIDException(
    message: String? = null,
    cause: Throwable? = null
) : IPFSException(
    message,
    cause
) {
    @StringRes
    override val errorMessage: Int = R.string.error_message_cid
}