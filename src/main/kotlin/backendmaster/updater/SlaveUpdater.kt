package backendmaster.updater

import backendmaster.networkmaster.MasterNetworkSocketHandler
import backendmaster.updater.interfaces.ISlaveUpdater
import enumstorage.update.ApplicationName
import enumstorage.update.UpdateInformation
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.UpdateProperties
import java.io.File
import java.lang.Exception
import java.util.*

object SlaveUpdater: ISlaveUpdater {
    private val properties = Properties()
    private val presetsStream = File(UpdateProperties.getSlaveUpdatePropertyPath()).inputStream()
    private val logger: Logger = LoggerFactory.getLogger(SlaveUpdater::class.java)

    @Synchronized
    override fun getUpdateInformation(): JSONObject {
        return if (isUpdateAvailable()) {
            setProperty(property = "slave.update", value = false.toString())
            propertystorage.UpdateInformation.getAsJson(applicationName = ApplicationName.Slave.name, changes = getChanges(), version = getVersion())
        } else {
            JSONObject()
        }
    }

    @Synchronized
    override fun checkForUpdates() {
        try {
            updateProperties()

            if (isUpdateAvailable()) {
                logger.info("Slave update is available. Sending update file ...")
                MasterNetworkSocketHandler.sendUpdateFile(file = getUpdateFile())
                logger.info("Slave update file sent")
            } else {
                logger.info("Slave update is NOT available")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred while running slave updater!\n${ex.message}")
        }
    }

    private fun updateProperties() = properties.load(presetsStream)

    private fun getUpdateFile(): File = File(UpdateProperties.getSlaveUpdateFilePath() + UpdateProperties.getSlaveUpdateFileName())

    private fun isUpdateAvailable(): Boolean = getProperty(property = "slave.update").toBoolean()

    private fun getVersion(): String = getProperty(property = "slave.version")

    private fun getChanges(): String = getProperty(property = "slave.changes")

    private fun getProperty(property: String): String = properties.getProperty(property)

    private fun setProperty(property: String, value: String): Any = properties.setProperty(property, value)
}