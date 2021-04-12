package com.github.evgeniikuznetsov.drill4jplugin.config

import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.*
import javax.swing.AbstractButton
import javax.swing.JPanel

class SettingsComponent {
    val panel: JPanel

    //    private val _remoteLogin = JBTextField()
//    private val _remotePassword = JBPasswordField()
//    private val _useCredential = JBCheckBox("the credentials are above")
    private val _useDefaultDir = JBCheckBox("in project dir")
    private val _remoteFileUrl = JBTextField()
    private val _projectDirPath = JBTextField()

//    var login: String
//        get() = _remoteLogin.text
//        set(newText) {
//            _remoteLogin.text = newText
//        }
//
//    var password: String
//        get() = _remotePassword.text.let { if (it.isNotBlank()) "your password" else it }
//        set(newText) {
//            _remotePassword.text = newText
//        }

//    var useCredentials: Boolean
//        get() = _useCredential.isSelected
//        set(boolean) {
//            _useCredential.isSelected = !boolean
//        }

    var filePath: String
        get() = _remoteFileUrl.text
        set(newText) {
            _remoteFileUrl.text = newText
        }

    var dirPath: String
        get() = _projectDirPath.text
        set(newText) {
            _projectDirPath.text = newText
        }

    var useDefaultDir: Boolean
        get() = !_useDefaultDir.isSelected
        set(boolean) {
            _useDefaultDir.isSelected = !boolean
        }

    init {
        panel = panel {
            titledRow("Drill4j plugin settings") {
                row("Remote file url: ") {
                    _remoteFileUrl()
                }
//                row("Login: ") {
//                    _remoteLogin()
//                }
//                row("Password: ") {
//                    _remotePassword()
//                }
//                row("Don't use credential") {
//                    _useCredential()
//                }
                row("Project dir path for save file: ") {
                    _projectDirPath().enableIf(_useDefaultDir.noSelected)
                }
                row("Use default dir") {
                    _useDefaultDir()
                }
            }
        }
    }
}

val AbstractButton.noSelected: ComponentPredicate
    get() = object : ComponentPredicate() {
        override fun invoke(): Boolean = !isSelected

        override fun addListener(listener: (Boolean) -> Unit) {
            addChangeListener { listener(!isSelected) }
        }
    }