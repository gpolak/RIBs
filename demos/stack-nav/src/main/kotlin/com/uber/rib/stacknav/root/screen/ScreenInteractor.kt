/*
 * Copyright (C) 2025. Uber Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.uber.rib.stacknav.root.screen

import com.uber.rib.core.Bundle
import com.uber.rib.core.EmptyPresenter
import com.uber.rib.core.Interactor

/**
 * Interactor for a single screen. Wires the view's button to [Listener.onPushNextScreen] and cleans
 * up on deactivation to avoid leaks.
 */
class ScreenInteractor(
  private val screenNumber: Int,
  private val listener: Listener,
) : Interactor<EmptyPresenter, ScreenRouter>(EmptyPresenter()) {

  interface Listener {
    fun onPushNextScreen(currentNumber: Int)
    fun onBackRequested()
  }

  override fun didBecomeActive(savedInstanceState: Bundle?) {
    super.didBecomeActive(savedInstanceState)
    router.view.onNextClicked = { listener.onPushNextScreen(screenNumber) }
    router.view.onBackClicked = { listener.onBackRequested() }
  }

  override fun willResignActive() {
    router.view.onNextClicked = null
    router.view.onBackClicked = null
    super.willResignActive()
  }
}
