package com.realityexpander.tasky.presentation.register_screen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.realityexpander.tasky.R
import com.realityexpander.tasky.common.Exceptions
import com.realityexpander.tasky.common.UiText
import com.realityexpander.tasky.domain.IAuthRepository
import com.realityexpander.tasky.domain.validation.IValidateEmail
import com.realityexpander.tasky.domain.validation.ValidatePassword
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_confirmPassword
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_email
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_errorMessage
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_isInvalidConfirmPassword
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_isInvalidEmail
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_isInvalidPassword
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_isLoggedIn
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_isPasswordsMatch
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_password
import com.realityexpander.tasky.presentation.common.UIConstants.SAVED_STATE_statusMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val validateEmail: IValidateEmail,
    private val validatePassword: ValidatePassword,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Get params from savedStateHandle (from another screen or after process death)
    private val email: String = savedStateHandle[SAVED_STATE_email] ?: ""
    private val password: String = savedStateHandle[SAVED_STATE_password] ?: ""
    private val confirmPassword: String = savedStateHandle[SAVED_STATE_confirmPassword] ?: ""
    private val isInvalidEmail: Boolean = savedStateHandle[SAVED_STATE_isInvalidEmail] ?: false
    private val isInvalidPassword: Boolean = savedStateHandle[SAVED_STATE_isInvalidPassword] ?: false
    private val isInvalidConfirmPassword: Boolean = savedStateHandle[SAVED_STATE_isInvalidConfirmPassword] ?: false
    private val isPasswordsMatch: Boolean = savedStateHandle[SAVED_STATE_isPasswordsMatch] ?: true
    private val isLoggedIn: Boolean = savedStateHandle[SAVED_STATE_isLoggedIn] ?: false
    private val statusMessage: UiText = savedStateHandle[SAVED_STATE_statusMessage] ?: UiText.None
    private val errorMessage: UiText = savedStateHandle[SAVED_STATE_errorMessage] ?: UiText.None

    private val _registerState = MutableStateFlow<RegisterState>(RegisterState())
    val registerState = _registerState.onEach { state ->
        // save state for process death
        savedStateHandle[SAVED_STATE_email] = state.email
        savedStateHandle[SAVED_STATE_password] = state.password
        savedStateHandle[SAVED_STATE_confirmPassword] = state.confirmPassword
        savedStateHandle[SAVED_STATE_isInvalidEmail] = state.isInvalidEmail
        savedStateHandle[SAVED_STATE_isInvalidPassword] = state.isInvalidPassword
        savedStateHandle[SAVED_STATE_isInvalidConfirmPassword] = state.isInvalidConfirmPassword
        savedStateHandle[SAVED_STATE_isPasswordsMatch] = state.isPasswordsMatch
        savedStateHandle[SAVED_STATE_isLoggedIn] = state.isLoggedIn
        savedStateHandle[SAVED_STATE_statusMessage] = state.statusMessage
        savedStateHandle[SAVED_STATE_errorMessage] = state.errorMessage

        // Only check for errors when the user clicks the login/register button
        //if(state.email.isNotBlank()) sendEvent(RegisterEvent.ValidateEmail(state.email))
        //if(state.password.isNotBlank()) sendEvent(RegisterEvent.ValidatePassword(state.password))
        //if(state.confirmPassword.isNotBlank()) sendEvent(RegisterEvent.ValidateConfirmPassword(state.confirmPassword))

        // Check for password match as the user types
        sendEvent(RegisterEvent.ValidatePasswordsMatch)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RegisterState())

    init {

        viewModelScope.launch {
            yield() // allow the registerState to be initialized

            // restore state after process death
            _registerState.value = RegisterState(
                email = email,
                password = password,
                confirmPassword = confirmPassword,
                isInvalidEmail = isInvalidEmail,
                isInvalidPassword = isInvalidPassword,
                isInvalidConfirmPassword = isInvalidConfirmPassword,
                isPasswordsMatch = isPasswordsMatch,
                isLoggedIn = isLoggedIn,
                statusMessage = statusMessage,
                errorMessage = errorMessage,
            )
            yield() // allow the registerState to be updated

            // Validate email & password only one-time when restored from process death or coming from another screen
            if (registerState.value.email.isNotBlank()) sendEvent(RegisterEvent.ValidateEmail)
            if (registerState.value.password.isNotBlank()) sendEvent(RegisterEvent.ValidatePassword)
            if (registerState.value.confirmPassword.isNotBlank()) sendEvent(RegisterEvent.ValidateConfirmPassword)
            sendEvent(RegisterEvent.ValidatePasswordsMatch)
        }
    }

    private suspend fun register(email: String, password: String) {
        try {
            val authToken = authRepository.register(email, password)
            sendEvent(RegisterEvent.RegisterSuccess(authToken))
        } catch(e: Exceptions.EmailAlreadyExistsException) {
            sendEvent(RegisterEvent.EmailAlreadyExists)
        } catch(e: Exceptions.LoginException) {
            sendEvent(RegisterEvent.RegisterError(UiText.Res(R.string.register_register_error, e.message ?: "")))
        } catch(e: Exceptions.InvalidEmailException) {
            sendEvent(RegisterEvent.IsValidEmail(false))
        } catch(e: Exceptions.InvalidPasswordException) {
            sendEvent(RegisterEvent.IsValidPassword(false))
        } catch (e: Exception) {
            sendEvent(RegisterEvent.UnknownError(UiText.Res( R.string.error_unknown, e.message ?: "")))
            e.printStackTrace()
        }
    }

    private fun validateEmail() {
        val isValid = validateEmail.validate(registerState.value.email)
        sendEvent(RegisterEvent.IsValidEmail(isValid))
    }

    private fun validatePassword() {
        val isValid = validatePassword.validate(registerState.value.password)
        sendEvent(RegisterEvent.IsValidPassword(isValid))
    }

    private fun validateConfirmPassword() {
        val isValid = validatePassword.validate(registerState.value.confirmPassword)
        sendEvent(RegisterEvent.IsValidConfirmPassword(isValid))
    }

    private fun validatePasswordsMatch() {
        // Both passwords must have at least 1 character to validate match
        if(registerState.value.password.isBlank()
            || registerState.value.confirmPassword.isBlank()
        ) {
            sendEvent(RegisterEvent.IsPasswordsMatch(true))

            return
        }

        val isMatch = (registerState.value.password == registerState.value.confirmPassword)
        sendEvent(RegisterEvent.IsPasswordsMatch(isMatch))
    }

    fun sendEvent(event: RegisterEvent) {
        viewModelScope.launch {
            onEvent(event)
            yield() // allow events to percolate
        }
    }

    private suspend fun onEvent(event: RegisterEvent) {
        when(event) {
            is RegisterEvent.Loading -> {
                _registerState.value = _registerState.value.copy(
                    isLoading = event.isLoading
                )
            }
            is RegisterEvent.UpdateEmail -> {
                _registerState.value = _registerState.value.copy(
                    email = event.email,
                    isInvalidEmail = false
                )
            }
            is RegisterEvent.UpdatePassword -> {
                _registerState.value = _registerState.value.copy(
                    password = event.password,
                    isInvalidPassword = false,
                    isPasswordsMatch = false
                )
            }
            is RegisterEvent.UpdateConfirmPassword -> {
                _registerState.value = _registerState.value.copy(
                    confirmPassword = event.confirmPassword,
                    isInvalidConfirmPassword = false,
                    isPasswordsMatch = false
                )
            }
            is RegisterEvent.TogglePasswordVisibility -> {
                _registerState.value = _registerState.value
                    .copy(isPasswordVisible = !event.isVisible)
            }
            is RegisterEvent.ValidateEmail -> {
                validateEmail()
                yield()
            }
            is RegisterEvent.ValidatePassword -> {
                validatePassword()
                yield()
            }
            is RegisterEvent.ValidateConfirmPassword -> {
                validateConfirmPassword()
                yield()
            }
            is RegisterEvent.ValidatePasswordsMatch -> {
                validatePasswordsMatch()
                yield()
            }
            is RegisterEvent.IsValidEmail -> {
                _registerState.value = _registerState.value.copy(
                    isInvalidEmail = !event.isValid
                )
            }
            is RegisterEvent.IsValidPassword -> {
                _registerState.value = _registerState.value.copy(
                    isInvalidPassword = !event.isValid
                )
            }
            is RegisterEvent.IsValidConfirmPassword -> {
                _registerState.value = _registerState.value.copy(
                    isInvalidConfirmPassword = !event.isValid
                )
            }
            is RegisterEvent.IsPasswordsMatch -> {
                _registerState.value = _registerState.value.copy(
                    isPasswordsMatch = event.isMatch
                )
            }
            is RegisterEvent.Register -> {
                sendEvent(RegisterEvent.ValidateEmail)
                sendEvent(RegisterEvent.ValidatePassword)
                sendEvent(RegisterEvent.ValidateConfirmPassword)
                sendEvent(RegisterEvent.ValidatePasswordsMatch)
                yield()

                if(_registerState.value.isInvalidEmail
                    || _registerState.value.isInvalidPassword
                    || _registerState.value.isInvalidConfirmPassword
                    || !registerState.value.isPasswordsMatch
                ) return

                sendEvent(RegisterEvent.Loading(true))
                register(event.email, event.password)
            }
            is RegisterEvent.EmailAlreadyExists -> {
                _registerState.value = _registerState.value.copy(
                    isLoggedIn = false,
                    errorMessage = UiText.Res(R.string.register_error_email_exists),
                    statusMessage = UiText.None,
                )
                sendEvent(RegisterEvent.Loading(false))
            }
            is RegisterEvent.RegisterSuccess -> {
                _registerState.value = _registerState.value.copy(
                    isLoggedIn = true,
                    errorMessage = UiText.None,
                    statusMessage = UiText.Res(R.string.register_success, event.authToken),
                    isPasswordVisible = false,
                )
                sendEvent(RegisterEvent.Loading(false))
            }
            is RegisterEvent.RegisterError -> {
                _registerState.value = _registerState.value.copy(
                    isLoggedIn = false,
                    errorMessage = event.message,
                    statusMessage = UiText.None,
                )
                sendEvent(RegisterEvent.Loading(false))
            }
            is RegisterEvent.UnknownError -> {
                _registerState.value = _registerState.value.copy(
                    errorMessage = event.message
                )
            }

        }
    }
}