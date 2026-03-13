package com.example.p2pnodereciever.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.p2pnodereciever.R
import com.example.p2pnodereciever.domain.IpfsStreamer
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class NodeViewModel(
    private val ipfsStreamer: IpfsStreamer,
) : ViewModel() {

    private val cid = MutableSharedFlow<String>()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state = cid
        .flatMapLatest { cidString ->
            ipfsStreamer.getBlocks(cidString)
        }
        .take(1)
        .map { array ->
            if (array.isEmpty()) {
                return@map NodeScreenState.Empty
            }
            NodeScreenState.Success(array.decodeToString())
        }.catch { ex ->
            ex.printStackTrace()
            emit(NodeScreenState.CidInput(R.string.error_message))
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            NodeScreenState.Empty
        )

    fun loadData(cidString: String) {
        viewModelScope.launch {
            cid.emit(cidString)
        }
    }


}

sealed interface NodeScreenState {
    data class CidInput(val error: Int? = null) : NodeScreenState
    data class Success(val data: String) : NodeScreenState
    data object Empty : NodeScreenState

}
