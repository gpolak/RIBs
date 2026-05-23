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
package com.uber.rib.stacknav.root

import com.uber.rib.core.BasicViewRouter
import com.uber.rib.core.RouterNavigator
import com.uber.rib.core.StackRouterNavigator
import com.uber.rib.stacknav.root.screen.ScreenInteractor
import com.uber.rib.stacknav.root.screen.ScreenRouter
import com.uber.rib.stacknav.root.screen.ScreenView

/**
 * Manages the screen stack using [StackRouterNavigator]. Each call to [pushScreen] pushes a new
 * [ScreenRouter] on top; [handleBackPress] pops it.
 */
class RootRouter(
  view: RootView,
  interactor: RootInteractor,
) : BasicViewRouter<RootView, RootInteractor>(view, interactor) {

  private val navigator: RouterNavigator<ScreenState> = StackRouterNavigator(this)

  fun pushScreen(number: Int) {
    val listener = interactor as ScreenInteractor.Listener
    navigator.pushState(
      ScreenState(number),
      object : RouterNavigator.AttachTransition<ScreenRouter, ScreenState> {
        override fun buildRouter(): ScreenRouter {
          val screenView = ScreenView(view.context, number, RootInteractor.MAX_SCREENS)
          return ScreenRouter(screenView, ScreenInteractor(number, listener))
        }

        override fun willAttachToHost(
          router: ScreenRouter,
          previousState: ScreenState?,
          newState: ScreenState,
          isPush: Boolean,
        ) {
          view.addView(
            router.view,
            android.view.ViewGroup.LayoutParams(
              android.view.ViewGroup.LayoutParams.MATCH_PARENT,
              android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            ),
          )
        }
      },
      object : RouterNavigator.DetachTransition<ScreenRouter, ScreenState> {
        override fun willDetachFromHost(
          router: ScreenRouter,
          previousState: ScreenState,
          newState: ScreenState?,
          isPush: Boolean,
        ) {
          view.removeView(router.view)
        }
      },
    )
  }

  override fun handleBackPress(): Boolean {
    if (navigator.size() > 1) {
      navigator.popState()
      return true
    }
    return false
  }

  fun popScreen() {
    navigator.popState()
  }

  override fun willDetach() {
    navigator.hostWillDetach()
    super.willDetach()
  }
}
