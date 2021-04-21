package com.github.evgeniikuznetsov.drill4jplugin.config

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

class SettingsConfigurable : Configurable {
    private lateinit var _settings: SettingsComponent

    override fun getDisplayName(): @Nls(capitalization = Nls.Capitalization.Title) String {
        return "Drill Plugin Settings"
    }

    override fun createComponent(): JComponent {
        return SettingsComponent().let {
            _settings = it
            return@let it.panel
        }
    }

    override fun isModified(): Boolean {
        return SettingsState.settings.let {
            val modified = it.remoteFilePath != _settings.adminUrl
                    || it.useDefaultDir != _settings.useDefaultDir || it.agentId != _settings.agentId || it.projectDirPath != _settings.dirPath
            modified
        }
    }

    override fun apply() {
        SettingsState.settings.let {
            it.remoteFilePath = this._settings.adminUrl
            it.useDefaultDir = this._settings.useDefaultDir
            it.agentId = this._settings.agentId
            it.projectDirPath = this._settings.dirPath
        }
    }

    override fun reset() {
        SettingsState.settings.let {
            this._settings.adminUrl = it.remoteFilePath
            this._settings.useDefaultDir = it.useDefaultDir
            this._settings.agentId = it.agentId
            this._settings.dirPath = it.projectDirPath
        }
    }
}
