package com.github.evgeniikuznetsov.drill4jplugin.config

import com.github.evgeniikuznetsov.drill4jplugin.config.constants.FtpTypes
import com.intellij.ide.passwordSafe.PasswordSafe
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.ServiceManager
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

@State(name = "com.epam.plugins.jspreact.configuration.AppSettingsState", storages = [Storage("Drill4jPluginsStorage.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var remoteFilePath = "Please, input path to coverage file"
    var useCredential = false
    var useDefaultDir = false
    var projectDirPath = "Please, input path to dir to download file"
    val credentials
        get() = PasswordSafe.instance

    override fun getState(): SettingsState {
        return this
    }

    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    companion object {
        val settings: SettingsState
            get() = ServiceManager.getService(SettingsState::class.java)
    }
}