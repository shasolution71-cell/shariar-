package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SomityDatabase
import com.example.data.model.MemberEntity
import com.example.data.model.MemberWithWeeks
import com.example.data.repository.SomityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterMode {
    ALL,
    ACTIVE_ONLY,
    OVERDUE_ONLY,
    DUE_ONLY
}

data class FinancialTotals(
    val memberCount: Int = 0,
    val activeMemberCount: Int = 0,
    val overdueMemberCount: Int = 0,
    val initialPrincipal: Long = 0L,
    val currentPrincipal: Long = 0L,
    val totalDebt: Long = 0L,
    val expectedProfit: Long = 0L,
    val receivedProfit: Long = 0L,
    val unpaidProfit: Long = 0L
)

class SomityViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SomityRepository

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime.asStateFlow()

    init {
        val db = SomityDatabase.getInstance(application)
        repository = SomityRepository(db.somityDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
        // Background ticker updates time every minute so countdown and auto-compounding update live daily
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(60_000L)
                _currentTime.value = System.currentTimeMillis()
            }
        }
    }

    val allMembers: StateFlow<List<MemberWithWeeks>> = repository.allMembers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    private val _selectedMemberId = MutableStateFlow<String?>(null)
    val selectedMemberId: StateFlow<String?> = _selectedMemberId.asStateFlow()

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _showSummaryDialog = MutableStateFlow(false)
    val showSummaryDialog: StateFlow<Boolean> = _showSummaryDialog.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Filtered list based on search and active filter mode, reacting to background time updates
    val displayedMembers: StateFlow<List<MemberWithWeeks>> = combine(
        allMembers,
        _searchQuery,
        _filterMode,
        _currentTime
    ) { list, query, filter, now ->
        val trimmed = query.trim().lowercase()
        var filtered = if (trimmed.isEmpty()) {
            list
        } else {
            list.filter {
                it.member.id.lowercase().contains(trimmed) ||
                it.member.phone.lowercase().contains(trimmed)
            }
        }

        when (filter) {
            FilterMode.ACTIVE_ONLY -> {
                filtered = filtered.filter { it.getCycleInfo(now).isActive }
            }
            FilterMode.OVERDUE_ONLY, FilterMode.DUE_ONLY -> {
                filtered = filtered.filter { it.getCycleInfo(now).isOverdue }
            }
            FilterMode.ALL -> {}
        }
        filtered
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Dynamic totals calculation including total debt and active/overdue counts
    val totals: StateFlow<FinancialTotals> = combine(allMembers, _currentTime) { list, now ->
        var initPrinc = 0L
        var currPrinc = 0L
        var debtSum = 0L
        var exp = 0L
        var rec = 0L
        var unp = 0L
        var activeCount = 0
        var overdueCount = 0

        list.forEach { m ->
            val cycle = m.getCycleInfo(now)
            if (cycle.isActive) activeCount++ else overdueCount++
            initPrinc += m.member.initialPrincipal
            currPrinc += cycle.currentPrincipal
            debtSum += cycle.totalDebt
            exp += m.totalExpected()
            rec += m.totalReceived()
            unp += m.totalUnpaid()
        }

        FinancialTotals(
            memberCount = list.size,
            activeMemberCount = activeCount,
            overdueMemberCount = overdueCount,
            initialPrincipal = initPrinc,
            currentPrincipal = currPrinc,
            totalDebt = debtSum,
            expectedProfit = exp,
            receivedProfit = rec,
            unpaidProfit = unp
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialTotals()
    )

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun setFilter(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun openAddMemberDialog() {
        _showAddDialog.value = true
    }

    fun closeAddMemberDialog() {
        _showAddDialog.value = false
    }

    fun openMemberDetail(memberId: String) {
        _selectedMemberId.value = memberId
    }

    fun closeMemberDetail() {
        _selectedMemberId.value = null
    }

    fun openSummaryDialog() {
        _showSummaryDialog.value = true
    }

    fun closeSummaryDialog() {
        _showSummaryDialog.value = false
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun addMember(
        id: String,
        initialPrincipal: Long,
        phone: String = "",
        photoUri: String = "",
        guarantor: String = "",
        startDate: Long = System.currentTimeMillis()
    ) {
        viewModelScope.launch {
            val result = repository.addMember(id, initialPrincipal, phone, photoUri, guarantor, startDate)
            result.onSuccess {
                _showAddDialog.value = false
                _snackbarMessage.value = "সদস্য $id সফলভাবে যোগ করা হয়েছে"
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "ত্রুটি হয়েছে"
            }
        }
    }

    fun updateMemberProfile(
        oldId: String,
        newId: String,
        phone: String,
        photoUri: String,
        guarantor: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val result = repository.updateMemberProfile(oldId, newId, phone, photoUri, guarantor)
            result.onSuccess {
                if (_selectedMemberId.value == oldId) {
                    _selectedMemberId.value = newId
                }
                _snackbarMessage.value = "প্রোফাইল সফলভাবে আপডেট করা হয়েছে"
                onSuccess?.invoke()
            }.onFailure { err ->
                _snackbarMessage.value = err.message ?: "আপডেট করা যায়নি"
            }
        }
    }

    fun addWeekRecord(memberId: String, expected: Long, received: Long, weekIndex: Int? = null) {
        viewModelScope.launch {
            repository.addWeekRecord(memberId, expected, received, weekIndex)
            val diff = expected - received
            if (diff > 0) {
                _snackbarMessage.value = "বাকি ৳$diff টাকার লাভ পরবর্তী আসলের সাথে যুক্ত হয়েছে"
            } else {
                _snackbarMessage.value = "সপ্তাহের হিসাব সংরক্ষিত হয়েছে (পরিশোধিত)"
            }
        }
    }

    fun deleteMember(member: MemberEntity) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (_selectedMemberId.value == member.id) {
                _selectedMemberId.value = null
            }
            _snackbarMessage.value = "সদস্য ${member.id} মুছে ফেলা হয়েছে"
        }
    }
}
