package com.savoria.app.ui.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn

fun <T> Flow<List<T>>.stateInAsListUiState(
    scope: CoroutineScope,
    started: SharingStarted = SharingStarted.WhileSubscribed(5_000)
): StateFlow<UiState<List<T>>> = flow {
    emit(UiState.Loading)
    collect { list ->
        emit(if (list.isEmpty()) UiState.Empty else UiState.Success(list))
    }
}.stateIn(scope, started, UiState.Loading)
