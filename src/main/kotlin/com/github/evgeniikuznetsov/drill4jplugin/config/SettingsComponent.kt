package com.github.evgeniikuznetsov.drill4jplugin.config

import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import javax.swing.*

class SettingsComponent {
    val panel: JPanel

    //TODO Find a way to specify current project path
    //private val _useDefaultDir = JBCheckBox("In project dir")
    private val _adminUrl = JBTextField()
    private val _projectDirPath = JBTextField()
    private val _agentId = JBTextField()
    private val _fromLocalFile = JBCheckBox()
    private val _pathToExistedFile = JBTextField()

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
// TODO
//    var useDefaultDir: Boolean
//        get() = !_useDefaultDir.isSelected
//        set(boolean) {
//            _useDefaultDir.isSelected = !boolean
//        }

    var pathToExistedFile: String
        get() = _pathToExistedFile.text
        set(newText) {
            _pathToExistedFile.text = newText
        }

    var fromLocalFile: Boolean
        get() = !_fromLocalFile.isSelected
        set(boolean) {
            _fromLocalFile.isSelected = !boolean
        }


    init {
        panel = panel {
            titledRow("Drill4j plugin settings") {
                row("Admin Url: ") {
                    _adminUrl().enableIf(_fromLocalFile.noSelected)
                }
                row("Agent Id: ") {
                    _agentId().enableIf(_fromLocalFile.noSelected)
                }

                row("Directory path for save file: ") {
                    _projectDirPath().enableIf(_fromLocalFile.noSelected)
                }
                // TODO
//                row("Use default dir") {
//                    _useDefaultDir().enableIf(_fromLocalFile.noSelected)
//                }

                row("Use local file") {
                    _fromLocalFile()
                }

                row("Local file path: ") {
                    _pathToExistedFile().enableIf(_fromLocalFile.selected)
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
val AbstractButton.selected: ComponentPredicate
    get() = object : ComponentPredicate() {
        override fun invoke(): Boolean = isSelected

        override fun addListener(listener: (Boolean) -> Unit) {
            addChangeListener { listener(isSelected) }
        }
    }
