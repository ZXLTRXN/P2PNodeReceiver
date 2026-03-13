package com.example.p2pnodereciever.domain

import kotlinx.coroutines.flow.Flow

interface IpfsStreamer {
    fun getBlocks(
        cidString: String,
    ): Flow<ByteArray>
}