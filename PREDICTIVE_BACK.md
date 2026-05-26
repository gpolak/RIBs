# Predictive Back Gesture Support

Android 14 (API 34) introduced the [Predictive Back gesture](https://developer.android.com/guide/navigation/custom-back/predictive-back-gesture), which shows a preview animation of where a back swipe will land before the user commits to it. Apps must explicitly opt in and use the modern back-navigation APIs to get these animations.

## What changed in RIBs

`RibActivity` previously intercepted back presses by overriding `onBackPressed()`, which is deprecated for back-interception purposes on Android 13+. It now registers an `OnBackPressedCallback` with `onBackPressedDispatcher` instead.

The internal RIBs back-handling chain is **unchanged**:

```
OnBackPressedCallback (in RibActivity)
  └─ Router.handleBackPress()
       └─ Interactor.handleBackPress(): Boolean
```

Interactors that override `handleBackPress()` and `ScreenStackBase` / `StackRouterNavigator` users require **no changes**.

## Opting in to Predictive Back animations

To enable the system back animations (swipe-to-home, cross-activity, cross-task), add the following to your app's `AndroidManifest.xml`:

```xml
<application
    android:enableOnBackInvokedCallback="true"
    ...>
```

This flag is what triggers the visual preview animations on Android 14+. Without it, back navigation continues to work exactly as before — the `RibActivity` change is fully backward-compatible regardless of this flag.

You can also opt individual activities in or out:

```xml
<activity
    android:name=".MyActivity"
    android:enableOnBackInvokedCallback="true" />
```

## Custom in-app back animations

If you want to drive your own animated preview (e.g., a custom route transition) during the back swipe, override `handleOnStarted`, `handleOnProgressed`, `handleOnCancelled`, and `handleOnBackPressed` in a custom `OnBackPressedCallback` and register it **before** the RIBs callback in your activity's `onCreate`:

```kotlin
class MyActivity : RibActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Register custom callback first so it sits ahead of RibActivity's in the chain.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Only intercept when your custom animation applies; otherwise disable
                // this callback so RibActivity's callback takes over.
                if (shouldAnimateCustomTransition()) {
                    runCustomBackAnimation()
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        })
        super.onCreate(savedInstanceState)
    }
}
```

## SDK requirements

| Requirement | Version |
|---|---|
| `androidx.activity` (transitive via `androidx.appcompat`) | 1.6.0+ |
| Predictive Back animations visible to users | Android 14+ (API 34) |
| `android:enableOnBackInvokedCallback` manifest flag | Android 13+ (API 33) — ignored on older OS versions |

No changes to your `build.gradle` files are needed. `androidx.appcompat:1.6.x` already provides the required `OnBackPressedCallback` API.

## Legacy behavior

Apps that do **not** add `android:enableOnBackInvokedCallback="true"` to their manifest are unaffected. Back navigation continues to work identically to before on all API levels. The change to `RibActivity` is purely internal.
