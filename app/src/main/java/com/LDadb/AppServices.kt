package com.LDadb

import android.content.Context
import com.LDadb.adb.KadbManager
import com.LDadb.adb.FastbootOtgManager
import com.LDadb.data.AppSettingsStore
import com.LDadb.data.RecentDeviceStore
import com.LDadb.discovery.AndroidAdbMdnsDiscovery
import com.LDadb.discovery.AdbMdnsDiscovery
import com.LDadb.discovery.LanAdbScanner
import com.LDadb.discovery.NetworkInfoProvider
import com.LDadb.download.NetworkDownloadManager
import com.LDadb.files.LocalFileManager
import com.LDadb.localapps.LocalAppExporter
import com.LDadb.repository.DefaultAdbRepository
import com.LDadb.scrcpy.ScrcpyRepository
import com.LDadb.usb.UsbOtgHost
import com.LDadb.usb.UsbOtgActions

object AppServices {
    private var appContext: Context? = null

    val context: Context
        get() = requireNotNull(appContext) { "AppServices 尚未初始化 Context" }

    val kadbManager: KadbManager by lazy { KadbManager() }
    val fastbootOtgManager: FastbootOtgManager by lazy { FastbootOtgManager() }
    val usbOtgHost: UsbOtgHost by lazy {
        UsbOtgHost(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val usbOtgActions: UsbOtgActions by lazy {
        UsbOtgActions(usbOtgHost)
    }
    val downloadManager: NetworkDownloadManager by lazy { NetworkDownloadManager(appContext) }
    val localFileManager: LocalFileManager by lazy {
        LocalFileManager(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val localAppExporter: LocalAppExporter by lazy {
        LocalAppExporter(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val settingsStore: AppSettingsStore by lazy {
        AppSettingsStore(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val recentDeviceStore: RecentDeviceStore by lazy {
        RecentDeviceStore(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val networkInfoProvider: NetworkInfoProvider by lazy {
        NetworkInfoProvider(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val lanAdbScanner: LanAdbScanner by lazy { LanAdbScanner() }
    val adbMdnsDiscovery: AdbMdnsDiscovery by lazy {
        AndroidAdbMdnsDiscovery(requireNotNull(appContext) { "AppServices 尚未初始化 Context" })
    }
    val adbRepository: DefaultAdbRepository by lazy {
        DefaultAdbRepository(kadbManager, fastbootOtgManager, usbOtgHost, recentDeviceStore, settingsStore)
    }
    val scrcpyRepository: ScrcpyRepository by lazy {
        ScrcpyRepository(requireNotNull(appContext) { "AppServices 尚未初始化 Context" }, kadbManager)
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }
}
