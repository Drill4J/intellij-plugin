package com.epam.drill.idea.plugin.config

import com.intellij.openapi.options.*
import org.jetbrains.annotations.*
import javax.swing.*

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
        return SettingsState.settings.run {
            adminUrl != _settings.adminUrl ||
                    // useDefaultDir != _settings.useDefaultDir ||
                    agentId != _settings.agentId ||
                    buildVersion != _settings.buildVersion ||
                    projectDirPath != _settings.dirPath ||
                    fromLocalFile != _settings.fromLocalFile ||
                    pathToExistedFile != _settings.pathToExistedFile
        }
    }

    override fun apply() {
        SettingsState.settings.run {
            adminUrl = _settings.adminUrl
            // useDefaultDir = _settings.useDefaultDir
            agentId = _settings.agentId
            buildVersion = _settings.buildVersion
            projectDirPath = _settings.dirPath
            fromLocalFile = _settings.fromLocalFile
            pathToExistedFile = _settings.pathToExistedFile
        }
    }

    override fun reset() {
        SettingsState.settings.run {
            _settings.adminUrl = adminUrl
            //_settings.useDefaultDir = useDefaultDir
            _settings.agentId = agentId
            _settings.buildVersion = buildVersion
            _settings.dirPath = projectDirPath
            _settings.fromLocalFile = fromLocalFile
            _settings.pathToExistedFile = pathToExistedFile
        }
    }
}
