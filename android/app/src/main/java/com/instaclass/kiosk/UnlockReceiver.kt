package com.instaclass.kiosk

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.UserManager

class UnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UNLOCK) return
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskAdminReceiver.componentName(context)
        if (!dpm.isDeviceOwnerApp(context.packageName)) return

        for (r in arrayOf(
            UserManager.DISALLOW_INSTALL_APPS,
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_UNINSTALL_APPS,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MODIFY_ACCOUNTS,
            UserManager.DISALLOW_FACTORY_RESET
        )) {
            try { dpm.clearUserRestriction(admin, r) } catch (_: Exception) {}
        }

        val pm = context.packageManager
        for (app in pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)) {
            try { dpm.setApplicationHidden(admin, app.packageName, false) } catch (_: Exception) {}
        }

        try { dpm.clearPackagePersistentPreferredActivities(admin, context.packageName) } catch (_: Exception) {}

        val mode = intent.getStringExtra("mode")
        if (mode == "release") {
            try { dpm.clearDeviceOwnerApp(context.packageName) } catch (_: Exception) {}
        }
    }

    companion object {
        const val ACTION_UNLOCK = "com.instaclass.kiosk.UNLOCK"
    }
}
