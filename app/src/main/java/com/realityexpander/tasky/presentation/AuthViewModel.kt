package com.realityexpander.tasky.presentation

import androidx.lifecycle.ViewModel
import com.realityexpander.tasky.common.Exceptions
import com.realityexpander.tasky.data.repository.AuthRepositoryImpl
import com.realityexpander.tasky.data.repository.local.AuthApiImpl
import com.realityexpander.tasky.data.repository.remote.AuthDaoImpl
import com.realityexpander.tasky.data.validation.ValidateEmailImpl
import com.realityexpander.tasky.domain.IAuthRepository

class AuthViewModel(
    private val authRepository: IAuthRepository = AuthRepositoryImpl(
        authApi = AuthApiImpl(),
        authDao = AuthDaoImpl(),
        validateEmail = ValidateEmailImpl()
    )
) : ViewModel() {

    suspend fun login(email: String, password: String) {
        try {
            authRepository.login(email, password)
        } catch(e: Exceptions.LoginException) {
            // _event.value = Event.LoginError
        } catch(e: Exceptions.InvalidEmailException) {
            // _event.value = Event.InvalidEmail
        } catch (e: Exception) {
            // handle general error
            //_event.value = Event.UnknownError(e.message)
            e.printStackTrace()
        }
    }

    suspend fun register(email: String, password: String) {
        try {
            authRepository.register(email, password)
        } catch (e: Exceptions.EmailAlreadyExistsException) {
            // _event.value = Event.EmailAlreadyExists
        } catch(e: Exceptions.InvalidEmailException) {
            // _event.value = Event.InvalidEmail
        } catch (e: Exception) {
            // handle general error
            //_event.value = Event.UnknownError(e.message)
            e.printStackTrace()
        }
    }
}