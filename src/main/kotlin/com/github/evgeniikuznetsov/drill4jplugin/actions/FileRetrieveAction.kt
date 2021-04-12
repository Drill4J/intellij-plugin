package com.github.evgeniikuznetsov.drill4jplugin.actions

import com.github.evgeniikuznetsov.drill4jplugin.config.SettingsState
import com.github.evgeniikuznetsov.drill4jplugin.tools.UrlFileRetriever
import com.intellij.codeInspection.ex.GlobalInspectionContextImpl
import com.intellij.coverage.*
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import org.apache.commons.lang3.StringUtils
import java.io.File

class FileRetrieveAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val remotePath = SettingsState.settings.remoteFilePath
        val agentId = SettingsState.settings.agentId
        val saveDir = "${SettingsState.settings.projectDirPath}/jacoco.exec"
        val socketFileRetriever = UrlFileRetriever(remotePath, agentId, saveDir)

        val project = e.getRequiredData(CommonDataKeys.PROJECT)
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
                val execFile = File(saveDir)
                if (!execFile.exists()) {
                    return
                }
                val extension = CoverageEngine.EP_NAME.extensions.first{
                    it.toString().contains("com.intellij.coverage.JavaCoverageEngine")
                }
                extension.toString()
                val coverageRunner =
                    CoverageRunner.EP_NAME.extensionList.find { StringUtils.equals(it?.dataFileExtension, "exec") }
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
                ).let { cs ->
                    cs!!.setCoverageData(coverageRunner.loadCoverageData(execFile, cs))
                    CoverageSuitesBundle(arrayOf(cs)).let {
                        extension.getCoverageAnnotator(project).renewCoverageData(it,
                            CoverageDataManager.getInstance(project).also { cdm -> cdm.chooseSuitesBundle(it) })
                    }
                }
            }
        })
    }
}
