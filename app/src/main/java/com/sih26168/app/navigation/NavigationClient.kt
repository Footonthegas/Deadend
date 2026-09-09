package com.sih26168.app.navigation

interface NavigationClient {
    fun processInput(input: NavigationInput): NavigationOutput
}
