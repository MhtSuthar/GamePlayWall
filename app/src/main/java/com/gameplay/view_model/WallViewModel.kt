package com.gameplay.view_model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class WallViewModel : ViewModel() {
    var v1 by mutableStateOf("0")
    var v2 by mutableStateOf("0")
    /*val result = snapshotFlow { v1 to v2 }.mapLatest {
        sum(it.first, it.second)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "0")*/
}