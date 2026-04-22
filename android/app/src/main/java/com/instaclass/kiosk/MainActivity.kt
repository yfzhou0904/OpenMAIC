package com.instaclass.kiosk

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.UserManager
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient

class MainActivity : Activity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                databaseEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                mediaPlaybackRequiresUserGesture = false
                useWideViewPort = true
                loadWithOverviewMode = true
            }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl(BuildConfig.KIOSK_URL)
        }
        setContentView(webView)

        configureDeviceOwnerPolicies()
        try { stopLockTask() } catch (_: Exception) {}
        enterImmersive()
    }

    override fun onResume() {
        super.onResume()
        try { stopLockTask() } catch (_: Exception) {}
        enterImmersive()
        hideOtherLauncherApps()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) enterImmersive()
    }

    @Suppress("DEPRECATION")
    private fun enterImmersive() {
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
    }

    private fun configureDeviceOwnerPolicies() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskAdminReceiver.componentName(this)
        if (!dpm.isDeviceOwnerApp(packageName)) return

        try { dpm.setLockTaskPackages(admin, arrayOf()) } catch (_: SecurityException) {}

        try {
            dpm.clearPackagePersistentPreferredActivities(admin, packageName)
            val filter = IntentFilter(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                addCategory(Intent.CATEGORY_DEFAULT)
            }
            dpm.addPersistentPreferredActivity(
                admin,
                filter,
                ComponentName(this, MainActivity::class.java)
            )
        } catch (_: SecurityException) {}

        for (r in arrayOf(
            UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,
            UserManager.DISALLOW_UNINSTALL_APPS,
            UserManager.DISALLOW_ADD_USER,
            UserManager.DISALLOW_MODIFY_ACCOUNTS
        )) {
            try { dpm.addUserRestriction(admin, r) } catch (_: SecurityException) {}
        }

        hideOtherLauncherApps()
    }

    private fun hideOtherLauncherApps() {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val admin = KioskAdminReceiver.componentName(this)
        if (!dpm.isDeviceOwnerApp(packageName)) return

        val keep = setOf(
            packageName,
            "com.android.settings",
            "com.android.documentsui",
            "com.google.android.packageinstaller",
            "com.android.packageinstaller"
        )

        val pm = packageManager
        val installed = pm.getInstalledApplications(PackageManager.MATCH_UNINSTALLED_PACKAGES)
        for (app in installed) {
            val pkg = app.packageName
            if (pkg in keep) continue
            if (pm.getLaunchIntentForPackage(pkg) == null) continue
            try {
                dpm.setApplicationHidden(admin, pkg, true)
            } catch (_: SecurityException) {}
            catch (_: IllegalArgumentException) {}
        }
    }
}
