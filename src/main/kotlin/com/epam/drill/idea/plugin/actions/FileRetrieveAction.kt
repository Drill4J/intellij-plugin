package com.epam.drill.idea.plugin.actions

import com.epam.drill.idea.plugin.config.*
import com.epam.drill.idea.plugin.tools.*
import com.intellij.codeInspection.ex.*
import com.intellij.coverage.*
import com.intellij.notification.*
import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import java.io.*
import java.nio.file.*

class FileRetrieveAction : AnAction() {

    private val extension = CoverageEngine.EP_NAME.extensions.first {
        "$it".contains("com.intellij.coverage.JavaCoverageEngine")
    }

    private val coverageRunners = CoverageRunner.EP_NAME.extensionList


    override fun actionPerformed(event: AnActionEvent) {
        if (!SettingsState.settings.fromLocalFile) {
            getCoverageFromLocalFile(event)
        } else {
            getCoverageFromRemoteHost(event)
        }
    }

    private fun getCoverageFromLocalFile(event: AnActionEvent) {
        val localFilePath = SettingsState.settings.pathToExistedFile

        val project = event.getRequiredData(CommonDataKeys.PROJECT)
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Importing file") {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Importing .exec file"
                val status = FileRetrieveStatus.SUCCESS.takeIf { Files.exists(Paths.get(localFilePath)) }
                    ?: FileRetrieveStatus.CAN_NOT_FIND_FILE

                GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(
                    status.message(),
                    status.notificationType
                ).notify(project)
            }

            override fun onFinished() {
                super.onFinished()
                displayCoverageFromFile(localFilePath, project, File(localFilePath))
            }
        })
    }

    private fun getCoverageFromRemoteHost(event: AnActionEvent) {
        val remotePath = SettingsState.settings.remoteFilePath
        val agentId = SettingsState.settings.agentId
        val buildVersion = SettingsState.settings.buildVersion
        val jacocoPath = "${SettingsState.settings.projectDirPath}\\jacoco.exec"
        val socketFileRetriever = UrlFileRetriever(remotePath, agentId, buildVersion, jacocoPath)

        val project = event.getRequiredData(CommonDataKeys.PROJECT)

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Download file") {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Download .exec file"

                socketFileRetriever.retrieveFile().let {
                    GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(
                        it.message(),
                        it.notificationType
                    ).notify(project)
                }
            }

            override fun onFinished() {
                super.onFinished()
                val execFile = File(jacocoPath)
                if (!execFile.exists()) {
                    return
                }
                displayCoverageFromFile(jacocoPath, project, execFile)
            }
        })
    }

    private fun displayCoverageFromFile(
        saveDir: String,
        project: Project,
        execFile: File,
    ) {

        runCatching {
            val coverageRunner = coverageRunners.find { it.dataFileExtension == "exec" }
                ?: throw NoSuchElementException("Collection contains no element matching the predicate.")
            extension.createCoverageSuite(
                coverageRunner, "Drill4j Plugin", DefaultCoverageFileProvider(File(saveDir)),
                arrayOfNulls(0),
                System.currentTimeMillis(),
                null,
                true,
                true,
                false,
                project
            )?.let { cs ->
                cs.setCoverageData(coverageRunner.loadCoverageData(execFile, cs))
                CoverageSuitesBundle(arrayOf(cs)).let {
                    extension.getCoverageAnnotator(project).renewCoverageData(it,
                        CoverageDataManager.getInstance(project).also { cdm ->
                            cdm.chooseSuitesBundle(it)
                            cdm.addSuiteListener(HideCoverageListener(project), cdm as Disposable)
                        })
                }
            }
        }.onFailure {
            GlobalInspectionContextImpl.NOTIFICATION_GROUP.createNotification(
                "${it.message}",
                NotificationType.ERROR
            ).notify(project)
        }
    }

}

class HideCoverageListener(
    private val project: Project,
) : CoverageSuiteListener {

    override fun afterSuiteChosen() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        val openFiles = fileEditorManager.openFiles
        openFiles.filterNotNull().forEach { openFile ->
            fileEditorManager.getAllEditors(openFile).filter { it as? TextEditor != null }.forEach {
                (it as TextEditor).editor.settings.isLineMarkerAreaShown = true
            }
        }
    }

    override fun beforeSuiteChosen() {
        val fileEditorManager = FileEditorManager.getInstance(project)
        val openFiles = fileEditorManager.openFiles
        openFiles.filterNotNull().forEach { openFile ->
            fileEditorManager.getAllEditors(openFile).filter { it as? TextEditor != null }.forEach {
                (it as TextEditor).editor.settings.isLineMarkerAreaShown = false
            }
        }
    }
}
