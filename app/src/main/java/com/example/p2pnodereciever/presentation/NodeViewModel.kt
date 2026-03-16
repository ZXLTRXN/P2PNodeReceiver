package com.example.p2pnodereciever.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pnodereciever.R
import com.example.p2pnodereciever.domain.IPFSException
import com.example.p2pnodereciever.domain.IpfsStreamer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch

class NodeViewModel(
    private val ipfsStreamer: IpfsStreamer,
) : ViewModel() {

    private val cid = MutableSharedFlow<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = cid
        .mapToNodeState(ipfsStreamer)
        .withPing(ipfsStreamer)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NodeScreenState.CidInput
        )

    fun loadData(cidString: String) {
        viewModelScope.launch {
            cid.emit(cidString)
        }
    }

}

sealed interface NodeScreenState {
    data object CidInput : NodeScreenState
    data class Success(
        val data: String,
        val ping: String = "",
        val pingError: Int? = null
    ) : NodeScreenState

    data object Empty : NodeScreenState
    data object Loading : NodeScreenState
    data class Error(val errorMessage: Int) : NodeScreenState
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<String>.mapToNodeState(
    ipfsStreamer: IpfsStreamer,
    loggerTag: String? = "NodeViewModel"
): Flow<NodeScreenState> = this.transformLatest { cidString ->
    emit(NodeScreenState.Loading)

    val blocks: List<ByteArray> = ipfsStreamer.getBlocks(cidString)
    if (blocks.isEmpty()) {
        emit(NodeScreenState.Empty)
    } else {

        val cleanString = blocks[0].decodeToString().replace(
            "\uFFFD", // Malformed byte sequences are replaced by this in decodeToString()
            ""
        ).trim()
        emit(NodeScreenState.Success(cleanString))
    }
}.catch { th ->
    Log.e(
        loggerTag,
        "getBlocks transformation error",
        th
    )
    val textRes = (th as? IPFSException)?.errorMessage ?: R.string.error_message
    emit(NodeScreenState.Error(textRes))
}

@OptIn(ExperimentalCoroutinesApi::class)
fun Flow<NodeScreenState>.withPing(
    ipfsStreamer: IpfsStreamer,
    loggerTag: String? = "NodeViewModel"
): Flow<NodeScreenState> = this.flatMapLatest { status ->
    if (status !is NodeScreenState.Success) {
        return@flatMapLatest flowOf(status)
    }

    ipfsStreamer.ping().map { result ->
        when {
            result.isSuccess -> {
                status.copy(ping = result.getOrNull()?.toString() ?: "")
            }

            result.isFailure -> {
                val th = result.exceptionOrNull()
                Log.e(
                    loggerTag,
                    "ping transformation error",
                    th
                )

                val textRes = (th as? IPFSException)?.errorMessage
                    ?: R.string.error_message

                status.copy(
                    ping = "",
                    pingError = textRes
                )
            }

            else -> status
        }
    }
}