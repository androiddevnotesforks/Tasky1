package com.realityexpander.tasky

import androidx.compose.material.Surface
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import com.realityexpander.tasky.auth_feature.data.repository.authRepositoryImpls.AuthRepositoryFake
import com.realityexpander.tasky.auth_feature.data.repository.local.authDaoImpls.AuthDaoFake
import com.realityexpander.tasky.auth_feature.data.repository.remote.authApiImpls.AuthApiFake
import com.realityexpander.tasky.auth_feature.domain.IAuthRepository
import com.realityexpander.tasky.auth_feature.domain.validation.ValidateEmail
import com.realityexpander.tasky.auth_feature.domain.validation.ValidatePassword
import com.realityexpander.tasky.auth_feature.domain.validation.ValidateUsername
import com.realityexpander.tasky.auth_feature.presentation.login_screen.LoginEvent
import com.realityexpander.tasky.auth_feature.presentation.login_screen.LoginScreen
import com.realityexpander.tasky.auth_feature.presentation.login_screen.LoginViewModel
import com.realityexpander.tasky.core.data.settings.AppSettingsRepositoryFake
import com.realityexpander.tasky.core.util.internetConnectivityObserver.ConnectivityObserverFake
import com.realityexpander.tasky.core.presentation.theme.TaskyTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AppTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var authRepository: IAuthRepository
    private val authApiFake = AuthApiFake()
    private val authDaoFake = AuthDaoFake()
    private val validateEmail = ValidateEmail()
    private val validatePassword = ValidatePassword()
    private val validateUsername = ValidateUsername()
    private val appSettingsRepository = AppSettingsRepositoryFake()
    private val connectivityObserver = ConnectivityObserverFake()

    @Before
    fun setUp() {
        authRepository = AuthRepositoryFake(
            authApi = authApiFake,
            authDao = authDaoFake,
            validateUsername = validateUsername,
            validateEmail = validateEmail,
            validatePassword = validatePassword,
        )

        loginViewModel = LoginViewModel(
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle(),
            validateEmail = validateEmail,
            validatePassword = validatePassword,
            appSettingsRepository = appSettingsRepository,
            connectivityObserver = connectivityObserver
        )
    }

    @Test
    fun app_launches() {
        composeTestRule.setContent {
            TaskyTheme {
                Surface {
                    LoginScreen(
                        navigator = EmptyDestinationsNavigator,
                        viewModel = loginViewModel
                    )
                }
            }
        }

        // Check app launches at the correct destination
        composeTestRule.onNodeWithText("LOG IN").assertIsDisplayed()
    }

    @Test
    fun app_login_shows_email_and_password_hidden_by_default() {
        // ARRANGE
        val expectedEmail = "chris@demo.com"
        val password = "1234567Aa"
        val expectedPassword = "•••••••••"
        loginViewModel = LoginViewModel(
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle().apply {
                set("email", "chris@demo.com")
                set("password", "1234567Aa")
            },
            validateEmail = validateEmail,
            validatePassword = validatePassword,
            appSettingsRepository = appSettingsRepository,
            connectivityObserver = connectivityObserver
        )

        loginViewModel.sendEvent(LoginEvent.UpdateEmail(expectedEmail))
        loginViewModel.sendEvent(LoginEvent.UpdatePassword(password))

        // ACT
        composeTestRule.setContent {
            TaskyTheme {
                Surface {
                    LoginScreen(
                        navigator = EmptyDestinationsNavigator,
                        viewModel = loginViewModel
                    )
                }
            }
        }

        // ASSERT
        composeTestRule.onNodeWithText(expectedEmail).assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedPassword).assertIsDisplayed()
    }

    @Test
    fun app_login_shows_email_and_password_unhidden_when_show_password_is_clicked() {

        // ARRANGE
        val expectedEmail = "chris@demo.com"
        val expectedPassword = "1234567Aa"
        loginViewModel = LoginViewModel(
            authRepository = authRepository,
            savedStateHandle = SavedStateHandle().apply {  // todo put back after Compose-Destination fix
                set("email", "chris@demo.com")
                set("password", "1234567Aa")
            },
            validateEmail = validateEmail,
            validatePassword = validatePassword,
            appSettingsRepository = appSettingsRepository,
            connectivityObserver = connectivityObserver
        )

        loginViewModel.sendEvent(LoginEvent.UpdateEmail(expectedEmail))
        loginViewModel.sendEvent(LoginEvent.UpdatePassword(expectedPassword))

        // ACT
        composeTestRule.setContent {
            TaskyTheme {
                Surface {
                    LoginScreen(
                        navigator = EmptyDestinationsNavigator,
                        viewModel = loginViewModel,
                        email = expectedEmail,
                        password = expectedPassword,
                    )
                }
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithContentDescription("Show password").assertExists()
        composeTestRule.onNodeWithContentDescription("Show password").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Show password").performClick()
        composeTestRule.waitForIdle()

        // ASSERT
        composeTestRule.onNodeWithText(expectedEmail).assertIsDisplayed()
        composeTestRule.onNodeWithText(expectedPassword).assertIsDisplayed()
    }

}
