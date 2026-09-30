package com.example.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper

/**
 * Traverses context wrappers to safely resolve the enclosing Activity.
 * Needed in Compose where LocalContext.current can be a ContextWrapper.
 */
fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
