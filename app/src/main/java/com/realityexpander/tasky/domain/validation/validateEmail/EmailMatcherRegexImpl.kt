package com.realityexpander.tasky.domain.validation.validateEmail

// Matches how the email is validated on the server
class EmailMatcherRegexImpl: IEmailMatcher {
    override fun matches(email: String): Boolean {
        // use regex to validate email
        val regex = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", RegexOption.IGNORE_CASE)
        return regex.matches(email)
    }
}