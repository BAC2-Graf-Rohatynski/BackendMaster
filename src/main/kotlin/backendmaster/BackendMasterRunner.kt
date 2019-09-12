package backendmaster

import backendmaster.command.CommandSocketHandler
import backendmaster.runner.ApplicationRunner
import backendmaster.updater.AutoUpdater
import enumstorage.update.ApplicationName
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object BackendMasterRunner {
    private val logger: Logger = LoggerFactory.getLogger(BackendMasterRunner::class.java)

    @Volatile
    private var runApplication = true

    fun start() {
        logger.info("Starting application")
        CommandSocketHandler
        AutoUpdater.checkForUpdates()
        ApplicationRunner.startAllApplications()
    }

    @Synchronized
    fun isRunnable(): Boolean = runApplication

    fun stop() {
        logger.info("Stopping application")
        runApplication = false

        CommandSocketHandler.closeSockets()
    }

    fun getUpdateInformation(): JSONObject = UpdateInformation.getAsJson(applicationName = ApplicationName.Backend.name)
}