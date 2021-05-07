package com.github.evgeniikuznetsov.drill4jplugin.actions

import com.github.evgeniikuznetsov.drill4jplugin.config.*
import com.github.evgeniikuznetsov.drill4jplugin.tools.*
import com.intellij.codeInspection.ex.*
import com.intellij.coverage.*
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import java.io.*
import java.nio.file.*

class FileRetrieveAction : AnAction() {

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
        val extension = CoverageEngine.EP_NAME.extensions.first {
            "$it".contains("com.intellij.coverage.JavaCoverageEngine")
        }
        runCatching {
            val coverageRunner = CoverageRunner.EP_NAME.extensionList.find { it.dataFileExtension == "exec" }
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
                        CoverageDataManager.getInstance(project).also { cdm -> cdm.chooseSuitesBundle(it) })
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
