package com.github.kr328.clash

interface MainActivityClassProvider {
  val mainActivityClass: Class<*>
}

private var _mainActivityClassProvider: MainActivityClassProvider? = null

val mainActivityClassProvider: MainActivityClassProvider
  get() = _mainActivityClassProvider ?: error("MainActivityClassProvider has not been initialized")

fun initMainActivityClassProvider(provider: MainActivityClassProvider) {
  _mainActivityClassProvider = provider
}
