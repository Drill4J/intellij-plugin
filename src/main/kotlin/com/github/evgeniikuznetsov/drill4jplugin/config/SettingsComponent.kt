package com.github.evgeniikuznetsov.drill4jplugin.config

import com.github.evgeniikuznetsov.drill4jplugin.util.*
import com.intellij.codeInspection.ex.*
import com.intellij.notification.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.*
import com.intellij.ui.components.*
import com.intellij.ui.layout.*
import org.apache.http.impl.client.*
import javax.swing.*
import javax.swing.event.*

class SettingsComponent {
    val panel: JPanel

    //TODO Find a way to specify current project path
    //private val _useDefaultDir = JBCheckBox("In project dir")
    private val _adminUrl = JBTextField()
    private val _projectDirPath = JBTextField()
    private val _fromLocalFile = JBCheckBox()
    private val _pathToExistedFile = JBTextField()

    //TODO Replace this with a special template used for form creation
    private val _agentId = ComboBox<String>()
    private val _buildVersion = ComboBox<String>()

    var adminUrl: String
        get() = _adminUrl.text
        set(newText) {
            _adminUrl.text = newText
        }

    var agentId: String
        get() = _agentId.selectedItem as? String ?: ""
        set(item) {
            if (!(0 until _agentId.itemCount).any { _agentId.getItemAt(it) == item })
                _agentId.addItem(item)
            _agentId.selectedItem = item
        }

    var buildVersion: String
        get() = _buildVersion.selectedItem as? String ?: ""
        set(item) {
            if (!(0 until _buildVersion.itemCount).any { _buildVersion.getItemAt(it) == item })
                _buildVersion.addItem(item)
            _buildVersion.selectedItem = item
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
                row("Agent build version: ") {
                    _buildVersion().enableIf(_fromLocalFile.noSelected)
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

        _agentId.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                runCatching {
                    _agentId.removeAllItems()
                    HttpClients.createDefault().use {
                        it.getAgentIds(adminUrl).forEach(_agentId::addItem)
                    }
                }.onFailure {
                    GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(
                        "Check the spelling of the admin url",
                        NotificationType.ERROR
                    ).notify(ProjectManager.getInstance().openProjects.first())
                }
            }

            override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {}
            override fun popupMenuCanceled(e: PopupMenuEvent) {}
        })
        _buildVersion.addPopupMenuListener(object : PopupMenuListener {
            override fun popupMenuWillBecomeVisible(e: PopupMenuEvent) {
                runCatching {
                    _buildVersion.removeAllItems()
                    HttpClients.createDefault().use {
                        it.getBuildVersions(adminUrl, agentId).forEach(_buildVersion::addItem)
                    }
                }.onFailure {
                    GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(
                        "Agent id is not set".takeIf { agentId.isEmpty() } ?: "Check the spelling of the admin url",
                        NotificationType.ERROR
                    ).notify(ProjectManager.getInstance().openProjects.first())
                }
            }

            override fun popupMenuWillBecomeInvisible(e: PopupMenuEvent?) {}
            override fun popupMenuCanceled(e: PopupMenuEvent) {}
        })


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
