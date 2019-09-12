package backendmaster.updater

import databaseclient.DatabaseClientRunner
import error.ErrorClientRunner
import rsaencryptionmodule.RsaEncryptionModuleRunner
import backendmaster.BackendMasterRunner
import backendmaster.git.GitHandler
import backendmaster.network.Connection
import backendmaster.path.MyPath
import backendmaster.runner.ApplicationRunner
import backendmaster.updater.interfaces.IOnlineUpdater
import databasemanager.DatabaseManagerRunner
import enumstorage.EnumStorageRunner
import errormanager.ErrorManagerRunner
import interfacehelper.InterfaceHelperRunner
import licensemanager.LicenseManagerRunner
import masternetworkmanager.MasterNetworkManagerRunner
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import propertystorage.PropertyStorageRunner
import synchmanager.SynchManagerRunner
import java.io.File

object OnlineUpdater: IOnlineUpdater {
    private val logger: Logger = LoggerFactory.getLogger(OnlineUpdater::class.java)

    @Synchronized
    override fun performUpdate(): JSONArray {
        logger.info("Executing online update ...")

        return try {
            if (!Connection.checkForInternetConnection()) {
                return JSONArray().put("No Internet connection! Online update cannot be done!")
            }

            if (!checkForLibsPath()) {
                return JSONArray().put("Firmware folder not existing")
            }

            ApplicationRunner.stopAllApplications()
            GitHandler.pullFirmwares()
            SlaveUpdater.checkForUpdates()

            val updateInformation = JSONArray()
            JSONArray()
                    .put(DatabaseClientRunner.getUpdateInformation())
                    .put(DatabaseManagerRunner.getUpdateInformation())
                    .put(EnumStorageRunner.getUpdateInformation())
                    .put(ErrorClientRunner.getUpdateInformation())
                    .put(ErrorManagerRunner.getUpdateInformation())
                    .put(InterfaceHelperRunner.getUpdateInformation())
                    .put(LicenseManagerRunner.getUpdateInformation())
                    .put(MasterNetworkManagerRunner.getUpdateInformation())
                    .put(PortHandlerRunner.getUpdateInformation())
                    .put(PropertyStorageRunner.getUpdateInformation())
                    .put(RsaEncryptionModuleRunner.getUpdateInformation())
                    .put(SynchManagerRunner.getUpdateInformation())
                    .put(BackendMasterRunner.getUpdateInformation())
                    .put(SlaveUpdater.getUpdateInformation())
                    .forEach { update ->
                        if (!(update as JSONObject).isEmpty) {
                            updateInformation.put(update)
                        }
                    }

            logger.info("Online update finished")
            ApplicationRunner.startAllApplications()
            updateInformation
        } catch (ex: Exception) {
            logger.error("Error occurred during executing online update!\n${ex.message}")
            ApplicationRunner.startAllApplications()
            JSONArray("Error occurred during executing online update: ${ex.message}")
        }
    }

    @Synchronized
    override fun hasUpdates(): JSONObject = if (Connection.checkForInternetConnection()) GitHandler.hasUpdates() else JSONObject()

    private fun checkForLibsPath(): Boolean = File(MyPath.getLibPath()).exists()
}