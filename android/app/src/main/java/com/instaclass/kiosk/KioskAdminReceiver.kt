package com.instaclass.kiosk

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context

class KioskAdminReceiver : DeviceAdminReceiver() {
    companion object {
        fun componentName(ctx: Context) = ComponentName(ctx, KioskAdminReceiver::class.java)
    }

    override fun onEnabled(context: Context, intent: android.content.Intent) {
        super.onEnabled(context, intent)
    }
}
