package com.contracts.app.domain

import android.app.Application
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.contracts.app.data.model.*
import com.contracts.app.data.repository.ExcelRepository
import com.contracts.app.ui.theme.AppColors
import com.contracts.app.ui.theme.with
import com.contracts.app.utils.FuzzySearch
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UiState(
    val contracts: List<Contract> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val search: String = "",
    val statusFilter: ContractStatus? = null,
    val showStarred: Boolean = false,
    val sortField: SortField = SortField.DAYS_REMAINING,
    val themeId: AppThemeId = AppThemeId.LIGHT,
    val customColors: AppColors? = null,
    val logoPath: String? = null,
    val myEmail: String = "",
    val notifyThresholds: Set<Int> = setOf(30, 60),
    val hasFile: Boolean = false,
)

class ContractViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = ExcelRepository(app)
    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    val filtered: StateFlow<List<Contract>> = state.map { s ->
        var list = s.contracts
        if (s.showStarred) list = list.filter { it.isStarred }
        if (s.statusFilter != null) list = list.filter { it.status == s.statusFilter }
        if (s.search.isNotBlank()) {
            list = list.map { c ->
                val score = maxOf(
                    FuzzySearch.score(s.search, c.title),
                    FuzzySearch.score(s.search, c.company),
                    FuzzySearch.score(s.search, c.contractNumber),
                )
                c to score
            }.filter { it.second > 0 }.sortedByDescending { it.second }.map { it.first }
        } else {
            list = when (s.sortField) {
                SortField.DAYS_REMAINING -> list.sortedBy { it.daysRemaining }
                SortField.TITLE          -> list.sortedBy { it.title }
                SortField.COMPANY        -> list.sortedBy { it.company }
                SortField.STATUS         -> list.sortedBy { it.status.ordinal }
            }
        }
        list
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val summary: StateFlow<Map<ContractStatus, Int>> = state.map { s ->
        ContractStatus.entries.associateWith { st -> s.contracts.count { it.status == st } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    init {
        _state.update {
            it.copy(
                themeId          = SettingsStore.loadThemeId(app),
                customColors     = SettingsStore.loadCustomColors(app),
                logoPath         = SettingsStore.loadLogoPath(app),
                myEmail          = SettingsStore.loadMyEmail(app),
                notifyThresholds = SettingsStore.loadNotifyThresholds(app),
            )
        }
        if (repo.hasCached()) {
            viewModelScope.launch {
                _state.update { it.copy(isLoading = true) }
                runCatching { repo.loadCached() }
                    .onSuccess { _state.update { s -> s.copy(contracts = it, isLoading = false, hasFile = true) } }
                    .onFailure { _state.update { s -> s.copy(isLoading = false, error = it.message) } }
            }
        }
    }

    fun loadFile(uri: Uri) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching { repo.saveAndParse(uri) }
                .onSuccess { _state.update { s -> s.copy(contracts = it, isLoading = false, hasFile = true) } }
                .onFailure { _state.update { s -> s.copy(isLoading = false, error = it.message) } }
        }
    }

    fun setSearch(q: String)               = _state.update { it.copy(search = q) }
    fun setFilter(s: ContractStatus?)      = _state.update { it.copy(statusFilter = s) }
    fun setShowStarred(v: Boolean)         = _state.update { it.copy(showStarred = v) }
    fun setSort(f: SortField)              = _state.update { it.copy(sortField = f) }
    fun clearError()                       = _state.update { it.copy(error = null) }

    fun setTheme(t: AppThemeId) {
        SettingsStore.saveThemeId(getApplication(), t)
        _state.update { it.copy(themeId = t) }
    }

    fun setNotifyThresholds(days: Set<Int>) {
        SettingsStore.saveNotifyThresholds(getApplication(), days)
        _state.update { it.copy(notifyThresholds = days) }
        com.contracts.app.notifications.DeadlineWorker.schedule(getApplication())
    }

    fun setMyEmail(email: String) {
        SettingsStore.saveMyEmail(getApplication(), email)
        _state.update { it.copy(myEmail = email) }
    }

    fun setLogoPath(path: String?) {
        SettingsStore.saveLogoPath(getApplication(), path)
        _state.update { it.copy(logoPath = path) }
    }

    /** یک اسلات رنگی مشخص از تم سفارشی رو تغییر می‌ده و ذخیره می‌کنه (و تم رو روی «سفارشی» می‌ذاره). */
    fun setCustomColor(slot: ColorSlot, color: Color) {
        val app: Application = getApplication()
        SettingsStore.saveCustomColor(app, slot, color)
        val updated = (_state.value.customColors ?: SettingsStore.loadCustomColors(app)).with(slot, color)
        SettingsStore.saveThemeId(app, AppThemeId.CUSTOM)
        _state.update { it.copy(customColors = updated, themeId = AppThemeId.CUSTOM) }
    }

    fun toggleStar(id: Int) = _state.update { s ->
        s.copy(contracts = s.contracts.map { if (it.id == id) it.copy(isStarred = !it.isStarred) else it })
    }

    fun saveNote(id: Int, note: String) = _state.update { s ->
        s.copy(contracts = s.contracts.map { if (it.id == id) it.copy(personalNote = note) else it })
    }
}
