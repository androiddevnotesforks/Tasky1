package com.realityexpander.tasky.presentation.register_screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.realityexpander.tasky.R
import com.realityexpander.tasky.common.UiText
import com.realityexpander.tasky.presentation.components.EmailField
import com.realityexpander.tasky.presentation.components.PasswordField
import com.realityexpander.tasky.presentation.destinations.LoginScreenDestination

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Destination("RegisterScreen")
fun RegisterScreen(
    email: String? = null,
    password: String? = null,
    confirmPassword: String? = null,
    navigator: DestinationsNavigator,
    viewModel: RegisterViewModel = hiltViewModel(),
) {

    val registerState by viewModel.registerState.collectAsState()
    val focusManager = LocalFocusManager.current

    fun performRegister() {
        viewModel.sendEvent(RegisterEvent.Register(
            registerState.email,
            registerState.password,
            registerState.confirmPassword
        ))

        focusManager.clearFocus()
    }

    fun navigateToLogin() {
        navigator.navigate(
            LoginScreenDestination(
                email = registerState.email,
                password = registerState.password,
                confirmPassword = registerState.confirmPassword
            )
        ) {
            popUpTo("RegisterScreen") {
                inclusive = true
            }
        }
    }

    BackHandler(true) {
        navigateToLogin()
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if(registerState.isLoading) {
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(
                modifier = Modifier.align(alignment = Alignment.Center)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(text = "Register:")
        Spacer(modifier = Modifier.height(8.dp))

        // EMAIL
        EmailField(
            value = registerState.email,
            isError = registerState.isInvalidEmail,
            onValueChange = {
                viewModel.sendEvent(RegisterEvent.UpdateEmail(it))
            }
        )
        if(registerState.isInvalidEmail) {
            Text(text = UiText.Res(R.string.error_invalid_email).get(), color = Color.Red)
        }
        Spacer(modifier = Modifier.height(8.dp))

        // PASSWORD
        PasswordField(
            value = registerState.password,
            isError = registerState.isInvalidPassword,
            onValueChange = {
                viewModel.sendEvent(RegisterEvent.UpdatePassword(it))
            },
            isPasswordVisible = registerState.isPasswordVisible,
            clickTogglePasswordVisibility = {
                viewModel.sendEvent(RegisterEvent.TogglePasswordVisibility(registerState.isPasswordVisible))
            },
            imeAction = ImeAction.Next,
        )
        if (registerState.isInvalidPassword) {
            Text(text = UiText.Res(R.string.error_invalid_password).get(), color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // CONFIRM PASSWORD
        PasswordField(
            label = UiText.Res(R.string.register_label_confirm_password).get(),
            placeholder = UiText.Res(R.string.register_placeholder_confirm_password).get(),
            value = registerState.confirmPassword,
            isError = registerState.isInvalidConfirmPassword,
            onValueChange = {
                viewModel.sendEvent(RegisterEvent.UpdateConfirmPassword(it))
            },
            isPasswordVisible = registerState.isPasswordVisible,
            clickTogglePasswordVisibility = {
                viewModel.sendEvent(RegisterEvent.TogglePasswordVisibility(registerState.isPasswordVisible))
            },
            imeAction = ImeAction.Done,
            doneAction = {
                performRegister()
            },
        )
        if (registerState.isInvalidConfirmPassword) {
            Text(text = UiText.Res(R.string.error_invalid_confirm_password).get(), color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // SHOW IF MATCHING PASSWORDS
        if(!registerState.isPasswordsMatch)
        {
            Text(text = UiText.Res(R.string.register_error_passwords_do_not_match).get(), color = Color.Red)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // SHOW PASSWORD REQUIREMENTS
        if(registerState.isInvalidPassword || registerState.isInvalidConfirmPassword) {
            Text(
                text = UiText.Res(R.string.register_password_requirements).get(),
                color = Color.Red
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // REGISTER BUTTON
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            performRegister()
        },
            enabled = !registerState.isLoading,
            modifier = Modifier
                .align(alignment = Alignment.End)
        ) {
            Text(text = UiText.Res(R.string.register_button).get())
            if(registerState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(16.dp)
                        .align(alignment = Alignment.CenterVertically)
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        // SIGN IN BUTTON
        Text(text = UiText.Res(R.string.register_already_a_member_sign_in).get(),
            color = Color.Cyan,
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .clickable(onClick = {
                    navigateToLogin()
                })
        )
        Spacer(modifier = Modifier.height(16.dp))

        // STATUS //////////////////////////////////////////

        registerState.errorMessage.getOrNull()?.let { errorMessage ->
            Text(
                text = "Error: $errorMessage",
                color = Color.Red,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        if(registerState.isLoggedIn) {
            Text(text = UiText.Res(R.string.register_registered).get())
            Spacer(modifier = Modifier.height(8.dp))
        }
        registerState.statusMessage.asStrOrNull()?.let { message ->
            Text(text = message)
            Spacer(modifier = Modifier.height(8.dp))
        }

    }
}