package com.example.p2pnodereciever.presentation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.p2pnodereciever.R

@Composable
fun NodeScreenStateful(
    modifier: Modifier = Modifier,
    viewModel: NodeViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    NodeScreen(
        modifier = modifier,
        state = state,
        onFetchByCid = {
            viewModel.loadData(it)
        }
    )
}

@Composable
fun NodeScreen(
    state: NodeScreenState,
    onFetchByCid: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val inputState = rememberTextFieldState("QmTBimFzPPP2QsB7TQGc2dr4BZD4i7Gm2X1mNtb6DqN9Dr")

    when (state) {
        is NodeScreenState.CidInput -> {
            FullScreenBox(
                modifier = modifier
            ) {
                BasicTextField(
                    state = inputState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.shapes.medium
                        )
                        .padding(8.dp)
                )
                Button(
                    onClick = {
                        onFetchByCid(inputState.text.toString())
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Text(stringResource(R.string.fetch))
                }
            }
        }

        is NodeScreenState.Empty -> {
            FullScreenBox(
                modifier = modifier
            ) {
                Text(stringResource(R.string.empty_message))
            }
        }

        is NodeScreenState.Success -> {
            FullScreenBox(
                modifier = modifier
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.data, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    Text(state.ping)
                    Spacer(Modifier.height(8.dp))
                    state.pingError?.let {
                        Text(stringResource(it),
                            color = MaterialTheme.colorScheme.error)
                    }

                }

            }
        }

        is NodeScreenState.Loading -> {
            FullScreenBox(
                modifier = modifier
            ) {
                CircularProgressIndicator()
            }
        }

        is NodeScreenState.Error -> {
            FullScreenBox(
                modifier = modifier
            ) {
                Text(stringResource(state.errorMessage))
            }
        }
    }
}

@Composable
fun FullScreenBox(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}