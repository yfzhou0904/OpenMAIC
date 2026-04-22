package com.instaclass.kiosk

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class PackageChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_PACKAGE_ADDED) return
        val pkg = intent.data?.schemeSpecificPart ?: return
        if (pkg == context.packageName) return

        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskAdminReceiver.componentName(context)
        if (!dpm.isDeviceOwnerApp(context.packageName)) return
        if (context.packageManager.getLaunchIntentForPackage(pkg) == null) return
        try { dpm.setApplicationHidden(admin, pkg, true) } catch (_: Exception) {}
    }
}
