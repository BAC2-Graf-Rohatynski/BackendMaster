package backendmaster.updater

import databaseclient.DatabaseClientRunner
import error.ErrorClientRunner
import rsaencryptionmodule.RsaEncryptionModuleRunner
import backendmaster.BackendMasterRunner
import backendmaster.path.MyPath
import backendmaster.runner.ApplicationRunner
import backendmaster.updater.interfaces.IUsbUpdater
import backendmaster.zip.ZipHandler
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
import propertystorage.UpdateProperties
import synchmanager.SynchManagerRunner
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object UsbUpdater: IUsbUpdater {
    private val logger: Logger = LoggerFactory.getLogger(UsbUpdater::class.java)

    @Synchronized
    override fun performUpdate(): JSONArray {
        logger.info("Executing offline update ...")

        return try {
            ApplicationRunner.stopAllApplications()

            if (!checkForLibsPath()) {
                return JSONArray().put("Firmware folder not '${MyPath.getLibPath()}' existing")
            }

            if (!checkForUpdatePath()) {
                return JSONArray().put("Update path or folder '${getUpdateFilePath()}' not existing")
            }

            copyUpdatePackageToLibFolder()
            unzipUpdatePackage()
            deleteUpdatePackage()
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

            logger.info("Offline update finished")
            ApplicationRunner.startAllApplications()
            updateInformation
        } catch (ex: Exception) {
            deleteUpdatePackage()
            logger.error("Error occurred during offline update!\n${ex.message}")
            ApplicationRunner.startAllApplications()
            JSONArray("Error occurred during offline update: ${ex.message}")
        }
    }

    @Synchronized
    override fun updatesAvailable(path: String?): Boolean = checkForUpdatePath()

    private fun copyUpdatePackageToLibFolder() {
        logger.info("Copying update package to libs folder ...")
        val sourceFile = File(getUpdateFilePath())
        Files.copy(Paths.get(sourceFile.toURI()), Paths.get(MyPath.getLibPath()))
        logger.info("Update package copied")
    }

    private fun unzipUpdatePackage() {
        logger.info("Unzipping update package ...")
        ZipHandler().unzip("${MyPath.getLibPath()}${UpdateProperties.getOfflinePackageName()}")
        logger.info("Update package unzipped")
    }

    private fun deleteUpdatePackage() {
        if (File("${MyPath.getLibPath()}${UpdateProperties.getOfflinePackageName()}").exists()) {
            File("${MyPath.getLibPath()}${UpdateProperties.getOfflinePackageName()}").delete()
        }
    }

    private fun checkForLibsPath(): Boolean = File(MyPath.getLibPath()).exists()

    private fun checkForUpdatePath(): Boolean = File(getUpdateFilePath()).exists()

    private fun getUpdateFilePath(): String = UpdateProperties.getOfflinePackagePath() + UpdateProperties.getOfflinePackageName()
}