package com.zerobook.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zerobook.app.data.AppDatabase
import com.zerobook.app.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class DashboardHeaderState(
    val fyLabel: String = "2025-26",
    val businessName: String = "",
    val gstin: String = ""
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getDatabase(application))

    val headerState: StateFlow<DashboardHeaderState> = repository.profile
        .map { profile ->
            DashboardHeaderState(
                fyLabel = profile?.fyLabel?.takeIf { it.isNotBlank() } ?: "2025-26",
                businessName = profile?.businessName.orEmpty(),
                gstin = profile?.gstin.orEmpty()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardHeaderState()
        )
}
