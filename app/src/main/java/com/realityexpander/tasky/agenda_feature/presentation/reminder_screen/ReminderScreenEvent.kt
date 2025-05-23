package com.realityexpander.tasky.agenda_feature.presentation.reminder_screen

import android.os.Parcelable
import androidx.compose.ui.text.TextStyle
import com.realityexpander.tasky.R
import com.realityexpander.tasky.agenda_feature.data.common.typeParceler.TextStyleParceler
import com.realityexpander.tasky.core.presentation.util.UiText
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler
import java.time.ZonedDateTime

enum class ShowAlertDialogActionType(val title: UiText) {
    DeleteReminder(UiText.Res(R.string.event_confirm_action_dialog_delete)),
    ConfirmOK(UiText.Res(android.R.string.ok)),
}

sealed interface ReminderScreenEvent {
    data class SetIsLoaded(val isLoaded: Boolean) : ReminderScreenEvent
    data class ShowProgressIndicator(val isVisible: Boolean) : ReminderScreenEvent

    // • Is Event Editable?
    data class SetIsEditable(val isEditable: Boolean) : ReminderScreenEvent

    // • The Current EditMode of Event (Title, Description, FromDateTime, ToDateTime, RemindAt, Photos)
    data class SetEditMode(val editMode: EditMode) : ReminderScreenEvent
    object CancelEditMode : ReminderScreenEvent

    // • Alert Dialog - Confirm Action (Delete/Join/Leave) & General-error-alerts
    data class ShowAlertDialog(
        val title: UiText,
        val message: UiText,
        val confirmButtonLabel: UiText = UiText.Res(android.R.string.ok),
        val onConfirm: () -> Unit,
        val isDismissButtonVisible: Boolean = true,
    ) : ReminderScreenEvent
    object DismissAlertDialog : ReminderScreenEvent

    // • Update/Save Reminder
    object SaveReminder : ReminderScreenEvent
    object DeleteReminder : ReminderScreenEvent

    // • Errors
    data class ShowErrorMessage(val message: UiText) : ReminderScreenEvent
    object ClearErrorMessage : ReminderScreenEvent

    // • Non-state One Time Events
    sealed interface OneTimeEvent {
        // • Event - Navigate Back to Previous Screen
        object NavigateBack : ReminderScreenEvent, OneTimeEvent

        data class ShowToast(val message: UiText) : ReminderScreenEvent, OneTimeEvent
    }

    sealed interface EditMode {

        // Dialog Display options
        val dialogTitle: UiText
        sealed interface EditTextStyle { // dialog uses a specific text style for edit text
            val editTextStyle: TextStyle
        }

        // • (1) WHICH item is being edited?
        // - sets initial/default value and the dialog display string)
        @Parcelize
        @TypeParceler<TextStyle, TextStyleParceler>()
        data class ChooseTitleText(
            override val text: String = "",
            override val dialogTitle: UiText = UiText.Res(R.string.event_dialog_title_choose_title_text),
            override val editTextStyle: TextStyle = TextStyle.Default
        ) : EditMode, EditTextStyle, TextPayload, Parcelable

        @Parcelize
        @TypeParceler<TextStyle, TextStyleParceler>()
        data class ChooseDescriptionText(
            override val text: String = "",
            override val dialogTitle: UiText = UiText.Res(R.string.event_dialog_title_choose_description_text),
            override val editTextStyle: TextStyle = TextStyle.Default
        ) : EditMode, EditTextStyle, TextPayload, Parcelable

        @Parcelize
        data class ChooseDate(
            override val dateTime: ZonedDateTime = ZonedDateTime.now(),
            override val dialogTitle: UiText = UiText.Res(R.string.event_dialog_title_choose_from_date)
        ) : EditMode, DateTimePayload, Parcelable
        @Parcelize
        data class ChooseTime(
            override val dateTime: ZonedDateTime = ZonedDateTime.now(),
            override val dialogTitle: UiText = UiText.Res(R.string.event_dialog_title_choose_from_time),
        ) : EditMode, DateTimePayload, Parcelable

        @Parcelize
        data class ChooseRemindAtDateTime(
            override val dateTime: ZonedDateTime = ZonedDateTime.now(),
            override val dialogTitle: UiText = UiText.Res(R.string.event_dialog_title_choose_remind_at_date_time)
        ) : EditMode, DateTimePayload, Parcelable


        // • (2) WHAT is being edited? (Text, DateTime, Photo, Attendee)
        sealed interface TextPayload {
            val text: String
        }
        sealed interface DateTimePayload {
            val dateTime: ZonedDateTime
        }


        // • (3) FINALLY "Update/Add/Remove Data" Events - Delivers the updated/added/removed data payload to the ViewModel
        data class UpdateText(override val text: String) : ReminderScreenEvent, TextPayload
        data class UpdateDateTime(override val dateTime: ZonedDateTime) : ReminderScreenEvent,
            DateTimePayload
    }
}
