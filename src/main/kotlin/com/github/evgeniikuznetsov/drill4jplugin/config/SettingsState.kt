package com.github.evgeniikuznetsov.drill4jplugin.config

import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "com.epam.plugins.jspreact.configuration.AppSettingsState", storages = [Storage("Drill4jPluginsStorage.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var remoteFilePath = "http://admin-url:8090"
    var useDefaultDir = false
    var agentId = "zeyt"
    var projectDirPath = "Please, specify the file path"

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
