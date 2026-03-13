package com.example.p2pnodereciever.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun NodeScreenStateful(
    modifier: Modifier = Modifier,
    viewModel: NodeViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NodeScreen(
        modifier = modifier,
        state = state,
        onButtonClick = {
            viewModel.loadData("QmTBimFzPPP2QsB7TQGc2dr4BZD4i7Gm2X1mNtb6DqN9Dr")
        }

    )
}

@Composable
fun NodeScreen(
    state: NodeScreenState,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is NodeScreenState.CidInput -> {
            Box(modifier = modifier) {
                if (state.error != null) Text("error")
                else Text("cid")
            }
        }

        is NodeScreenState.Empty -> {
            Box(modifier = modifier.fillMaxSize().padding(16.dp)) {
                Text("empty")
                Button(
                    onClick = {
                        onButtonClick()
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text("button")
                }
            }
        }

        is NodeScreenState.Success -> {
            Box(modifier = modifier) {
                Text(state.data)
            }
        }
    }

}