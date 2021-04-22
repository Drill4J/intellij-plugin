package com.github.evgeniikuznetsov.drill4jplugin.config

import com.intellij.ide.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.*
import com.intellij.openapi.externalSystem.autoimport.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*

@State(name = "com.epam.plugins.drill4j.configuration.AppSettingsState",
    storages = [Storage("Drill4jPluginsStorage.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var remoteFilePath = "http://admin-url:8090"
    //  var useDefaultDir = false
    var agentId = "ap02"
    var projectDirPath = "Please, specify the file path"
    var fromLocalFile = true
    var pathToExistedFile = "Please, specify the path to local coverage file"

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
