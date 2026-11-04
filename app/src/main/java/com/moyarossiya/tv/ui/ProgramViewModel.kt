package com.moyarossiya.tv.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moyarossiya.tv.data.MoyaApi
import com.moyarossiya.tv.data.ScheduleEntry
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class ProgramError {
    LIVE_INFO,
    SCHEDULE,
}

data class ProgramUiState(
    val nowTitle: String = "",
    val nextTitle: String = "",
    val scheduleDate: String = "",
    val entries: List<ScheduleEntry> = emptyList(),
    val isLoading: Boolean = true,
    val error: ProgramError? = null,
)

class ProgramViewModel : ViewModel() {
    private val api = MoyaApi.create()
    private val _uiState = MutableStateFlow(ProgramUiState())
    val uiState: StateFlow<ProgramUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                refreshNowAndNext()
                delay(15_000)
            }
        }
        viewModelScope.launch {
            while (isActive) {
                refreshSchedule()
                delay(60_000)
            }
        }
    }

    fun retry() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = it.entries.isEmpty()) }
            refreshNowAndNext()
            refreshSchedule()
        }
    }

    private suspend fun refreshNowAndNext() {
        runCatching {
            val now = api.now()
            val next = api.next()
            _uiState.update {
                it.copy(
                    nowTitle = now.titel.orEmpty(),
                    nextTitle = next.titel.orEmpty(),
                    isLoading = false,
                    error = null,
                )
            }
        }.onFailure {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = ProgramError.LIVE_INFO,
                )
            }
        }
    }

    private suspend fun refreshSchedule() {
        runCatching {
            val schedule = api.schedule()
            _uiState.update {
                it.copy(
                    scheduleDate = schedule.tag,
                    entries = schedule.eintraege,
                    isLoading = false,
                    error = null,
                )
            }
        }.onFailure {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = ProgramError.SCHEDULE,
                )
            }
        }
    }
}
