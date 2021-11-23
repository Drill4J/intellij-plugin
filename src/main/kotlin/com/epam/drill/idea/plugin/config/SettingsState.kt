package com.epam.drill.idea.plugin.config

import com.epam.drill.idea.plugin.util.*
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.*

@State(name = "com.epam.plugins.drill4j.configuration.AppSettingsState",
    storages = [Storage("Drill4jPluginsStorage.xml")])
class SettingsState : PersistentStateComponent<SettingsState?> {
    var adminUrl = "http://admin-url:8090"
    //  var useDefaultDir = false
    var agentId = "ap02"
    var buildVersion = "0.0.1"
    var projectDirPath = getProjectPath()
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
