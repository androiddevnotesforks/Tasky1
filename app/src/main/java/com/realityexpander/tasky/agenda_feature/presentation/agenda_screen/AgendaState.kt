package com.realityexpander.tasky.agenda_feature.presentation.agenda_screen

import com.realityexpander.tasky.agenda_feature.domain.AgendaItem
import com.realityexpander.tasky.auth_feature.domain.AuthInfo
import com.realityexpander.tasky.core.util.Email
import com.realityexpander.tasky.core.util.Username
import com.realityexpander.tasky.core.util.UuidStr
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

data class AgendaState(
    val username: Username = "",
    val email: Email = "",
    val authInfo: AuthInfo? = null,

    val isLoaded: Boolean = false, // true only after init() is called
    val isProgressVisible: Boolean = false,
    val isRefreshing: Boolean = false,

    val weekStartDate: ZonedDateTime = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS),
    val chooseWeekStartDateDialog: ZonedDateTime? = null,
    val selectedDayIndex: Int? = null,

    val agendaItems: List<AgendaItem> = emptyList<AgendaItem>(),

    // Stateful One-time events
    val scrollToItemId: UuidStr? = null,
    val scrollToTop: Boolean = false,
    val scrollToBottom: Boolean = false,
    val resetScrollTo: Boolean = false,

    val confirmDeleteAgendaItem: AgendaItem? = null,
)
