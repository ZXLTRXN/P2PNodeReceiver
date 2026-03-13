package com.example.p2pnodereciever.domain

import kotlinx.coroutines.flow.Flow

interface IpfsStreamer {
    suspend fun getBlocks(
        cidString: String,
    ): List<ByteArray>

    fun ping(
        repeatEveryMillis: Long = 2000L,
    ): Flow<Result<Long>>
}