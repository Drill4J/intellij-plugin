package com.github.evgeniikuznetsov.drill4jplugin.config

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
    private val _adminUrl = JBTextField()
    private val _projectDirPath = JBTextField()
    private val _agentId = JBTextField()

    var adminUrl: String
        get() = _adminUrl.text
        set(newText) {
            _adminUrl.text = newText
        }

    var agentId: String
        get() = _agentId.text
        set(newText) {
            _agentId.text = newText
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
                row("Admin Url: ") {
                    _adminUrl()
                }
                row("Agent Id: ") {
                    _agentId()
                }

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
