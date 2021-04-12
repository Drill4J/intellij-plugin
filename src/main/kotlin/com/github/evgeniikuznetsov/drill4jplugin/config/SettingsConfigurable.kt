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
            val modified = it.remoteFilePath != _settings.filePath
//                    || it.useCredential != _settings.useCredentials
                    || it.useDefaultDir != _settings.useDefaultDir || it.projectDirPath != _settings.dirPath
            modified
//            or (checkCredential(it))
        }
    }

//    private fun checkCredential(settings: SettingsState) =
//        Credentials(_settings.login, _settings.password) != settings.credentials[Constants.credentialAttribute]

    override fun apply() {
        SettingsState.settings.let {
            it.remoteFilePath = this._settings.filePath
//            it.useCredential = this._settings.useCredentials
            it.useDefaultDir = this._settings.useDefaultDir
            it.projectDirPath = this._settings.dirPath
//            it.credentials.set(Constants.credentialAttribute, Credentials(this._settings.login, this._settings.password))
        }
    }

    override fun reset() {
        SettingsState.settings.let {
            this._settings.filePath = it.remoteFilePath
//            this._settings.useCredentials = it.useCredential
            this._settings.useDefaultDir = it.useDefaultDir
            this._settings.dirPath = it.projectDirPath
//            it.credentials[Constants.credentialAttribute].let { cred ->
//                this._settings.login = cred?.userName ?: "Please, input your login"
//                this._settings.password = cred?.getPasswordAsString() ?: ""
//            }
        }
    }
}