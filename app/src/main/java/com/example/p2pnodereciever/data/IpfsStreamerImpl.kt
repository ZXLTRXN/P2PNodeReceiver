package com.example.p2pnodereciever.data

import com.example.p2pnodereciever.di.SimpleServiceLocator.Companion.ADDRESS
import com.example.p2pnodereciever.di.SimpleServiceLocator.Companion.PEER_ADDRESS
import com.example.p2pnodereciever.domain.IPFSCIDException
import com.example.p2pnodereciever.domain.IPFSException
import com.example.p2pnodereciever.domain.IPFSTimeoutException
import com.example.p2pnodereciever.domain.IpfsStreamer
import io.ipfs.cid.Cid
import io.libp2p.core.ConnectionClosedException
import io.libp2p.core.PeerId
import io.libp2p.core.multiformats.Multiaddr
import io.libp2p.protocol.PingController
import io.libp2p.protocol.PingProtocol
import io.libp2p.protocol.PingTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.future.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import org.peergos.EmbeddedIpfs
import org.peergos.Want
import kotlin.coroutines.cancellation.CancellationException


class IpfsStreamerImpl(
    private val ipfsCreate: () -> EmbeddedIpfs,
    private val ioDispatcher: CoroutineDispatcher
) : IpfsStreamer {

    override fun ping(
        repeatEveryMillis: Long,
    ): Flow<Result<Long>> = flow {
        val peerId = PeerId.fromBase58(PEER_ADDRESS)
        val multiaddr = Multiaddr(ADDRESS)

        val ipfs = ipfsCreate()

        val pingInitiator: PingController = try {
            ipfs.node.newStream<PingProtocol.PingInitiator>(
                listOf("/ipfs/ping/1.0.0"),
                peerId,
                multiaddr
            ).controller.await()
        } catch (ex: CancellationException) {
            ipfs.stopCatching()
            throw ex
        } catch (ex: Exception) {
            emit(Result.failure(IPFSException(cause = ex)))
            ipfs.stopCatching()
            return@flow
        }

        while (currentCoroutineContext().isActive) {
            try {
                val time = pingInitiator.ping().await()
                emit(Result.success(time))
            } catch (ex: CancellationException) {
                ipfs.stopCatching()
                throw ex
            } catch (ex: PingTimeoutException) {
                emit(
                    Result.failure(
                        IPFSTimeoutException(
                            cause = ex
                        )
                    )
                )
            } catch (ex: ConnectionClosedException) {
                emit(Result.failure(IPFSException(cause = ex)))
                ipfs.stopCatching()
                break
            } catch (ex: Exception) {
                emit(Result.failure(IPFSException(cause = ex)))
            }

            delay(repeatEveryMillis)
        }
        ipfs.stopCatching()

    }.flowOn(ioDispatcher)


    /**
     * @throws IPFSException если не удалось получить блоки
     * @throws IPFSCIDException если не удалось декодировать CID
     */
    override suspend fun getBlocks(
        cidString: String,
    ): List<ByteArray> = withContext(ioDispatcher) {
        val retrieveFrom: Set<PeerId> = setOf(PeerId.fromBase58(PEER_ADDRESS))
        val addToLocal = false // no need to save locally

        val cid = try {
            Cid.decode(cidString)
        } catch (ex: RuntimeException) {
            throw IPFSCIDException(cause = ex)
        }
        val ipfs = ipfsCreate()
        val wants = listOf(Want(cid))

        try {
            ipfs.start()
            val blocks = ipfs.getBlocks(
                wants,
                retrieveFrom,
                addToLocal
            )
            return@withContext blocks.map { it.block }

        } catch (ex: Exception) {
            throw IPFSException(cause = ex)
        } finally {
            ipfs.stopCatching()
        }
    }
}


fun EmbeddedIpfs.stopCatching() {
    try {
        stop()
    } catch (ex: Exception) {
        ex.printStackTrace()
    }
}