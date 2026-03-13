package com.example.p2pnodereciever.di

import com.example.p2pnodereciever.data.IpfsStreamerImpl
import com.example.p2pnodereciever.domain.IpfsStreamer
import io.ipfs.multiaddr.MultiAddress
import io.libp2p.core.PeerId
import io.libp2p.core.crypto.PrivKey
import kotlinx.coroutines.Dispatchers
import org.peergos.BlockRequestAuthoriser
import org.peergos.EmbeddedIpfs
import org.peergos.HostBuilder
import org.peergos.blockstore.RamBlockstore
import org.peergos.config.IdentitySection
import org.peergos.protocol.dht.RamRecordStore
import org.peergos.protocol.http.HttpProtocol.HttpRequestProcessor
import java.util.Optional
import java.util.concurrent.CompletableFuture

class SimpleServiceLocator {
    companion object {
        fun getIPFS(): EmbeddedIpfs {
            val swarmAddresses: List<MultiAddress> = listOf(
//                MultiAddress("/ip6/::/tcp/4001")
            )
            val bootstrapAddresses: List<MultiAddress> =
                listOf(
//                    MultiAddress("/dnsaddr/bootstrap.libp2p.io/p2p/QmQCU2EcMqAqQPR2i9bChDtGNJchTbq5TbXJJ16u19uLTa")
//                    MultiAddress("/dnsaddr/ipfs.infra.cf.team/p2p/12D3KooWKiqj21VphU2eE25438to5xeny6eP6d3PXT93ZczagPLT")
//                    MultiAddress("/dns4/ipfs.infra.cf.team/p2p/12D3KooWKiqj21VphU2eE25438to5xeny6eP6d3PXT93ZczagPLT")
                    MultiAddress("/ip4/95.143.188.166/tcp/4001/p2p/12D3KooWKiqj21VphU2eE25438to5xeny6eP6d3PXT93ZczagPLT")
                )
            val authorizer =
                BlockRequestAuthoriser { _, _, _ ->
                    CompletableFuture
                        .completedFuture(true)
                }
            val builder: HostBuilder = HostBuilder().generateIdentity()
            val privKey: PrivKey = builder.privateKey
            val peerId: PeerId = builder.peerId
            val identity = IdentitySection(
                privKey.bytes(),
                peerId
            )
            val provideBlocks = true

            return EmbeddedIpfs.build(
                RamRecordStore(),
                RamBlockstore(),
                provideBlocks,
                swarmAddresses,
                bootstrapAddresses,
                identity,
                authorizer,
                Optional.empty<HttpRequestProcessor>()
            )
        }

        val ipfsStreamer: IpfsStreamer
            get() {
                return IpfsStreamerImpl(
                    getIPFS(),
                    "12D3KooWKiqj21VphU2eE25438to5xeny6eP6d3PXT93ZczagPLT",
                    Dispatchers.IO
                )
            }
    }

}