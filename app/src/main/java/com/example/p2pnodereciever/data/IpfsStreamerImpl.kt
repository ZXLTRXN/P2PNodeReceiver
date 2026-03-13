package com.example.p2pnodereciever.data

import com.example.p2pnodereciever.domain.IpfsStreamer
import io.ipfs.cid.Cid
import io.libp2p.core.PeerId
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import org.peergos.EmbeddedIpfs
import org.peergos.Want
import kotlin.coroutines.cancellation.CancellationException


class IpfsStreamerImpl(
    private val ipfs: EmbeddedIpfs,
    private val peerAddress: String,
    private val ioDispatcher: CoroutineDispatcher
) : IpfsStreamer {

    /**
     * Стримит данные конкретного CID из указанного пира
     * @throws IllegalArgumentException fixme
     */
    override fun getBlocks(
        cidString: String,
    ): Flow<ByteArray> = callbackFlow {

        val wants = listOf(Want(Cid.decode(cidString)))

        val retrieveFrom: Set<PeerId> = setOf(PeerId.fromBase58(peerAddress))
        val addToLocal = true // no need to save locally

        try {
            ipfs.start()
            val blocks = ipfs.getBlocks(
                wants,
                retrieveFrom,
                addToLocal
            )

            for (hashedBlock in blocks) {
                channel.trySendBlocking(hashedBlock.block)
            }
            channel.close()
        } catch (ex: Exception) {
            cancel(
                CancellationException(
                    "Get blocks failed with Exception",
                    ex
                )
            )
        }

        awaitClose { ipfs.stop() }
    }.buffer(Channel.BUFFERED)
        .flowOn(ioDispatcher)
}