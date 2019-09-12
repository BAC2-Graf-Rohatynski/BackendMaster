package backendmaster.handler

import backendmaster.command.CommandSocket
import backendmaster.handler.interfaces.ICommandHandler
import backendmaster.runner.ApplicationRunner
import backendmaster.updater.*
import enumstorage.update.UpdateCommand
import enumstorage.update.UpdateCommandValues
import enumstorage.update.UpdateResponse
import interfacehelper.MyOperatingSystem
import org.json.JSONArray
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.Exception

object CommandHandler: ICommandHandler {
    private lateinit var commandSocket: CommandSocket
    private val logger: Logger = LoggerFactory.getLogger(CommandHandler::class.java)

    @Synchronized
    override fun parse(message: JSONObject) {
        when (message.getString(UpdateCommandValues.Command.name)) {
            UpdateCommand.UsbUpdate.name -> manualUpdateViaUsb()
            UpdateCommand.OnlineUpdate.name -> manualOnlineUpdate()
            UpdateCommand.AvailableOnlineUpdates.name -> getOnlineUpdates()
            UpdateCommand.EnableAutoOnlineUpdate.name -> enableAutoOnlineUpdate(message = message)
            UpdateCommand.EnableAutoOfflineUpdate.name -> enableAutoOfflineUpdate(message = message)
            UpdateCommand.IsAutoOnlineUpdateEnabled.name -> isAutoOnlineUpdateEnabled()
            UpdateCommand.IsAutoOfflineUpdateEnabled.name -> isAutoOfflineUpdateEnabled()
            UpdateCommand.Shutdown.name -> shutdownSystem()
            UpdateCommand.Restart.name -> restartApplications()
            else -> logger.error("Invalid command '${message.getString(UpdateCommandValues.Command.name)}' received!")
        }
    }
    
    private fun getUpdateResponse(command: UpdateCommand, result: Boolean): JSONObject = JSONObject()
            .put(UpdateResponse.UpdateCommand.name, command)
            .put(UpdateResponse.Result.name, result)

    private fun restartApplications() {
        try {
            logger.info("Restarting all applications ...")
            ApplicationRunner.stopAllApplications()
            // TODO: Restart backend master here
            ApplicationRunner.startAllApplications()
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.Restart, result = true))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.Restart.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.Restart, result = false))
        }
    }

    private fun shutdownSystem() {
        try {
            logger.info("Shutting down system ...")
            val shutdownCommand = MyOperatingSystem.getShutdownCommand() ?: throw Exception("Unsupported operating system '${MyOperatingSystem.getOperatingSystem()}' detected!")
            Runtime.getRuntime().exec(shutdownCommand)
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.Shutdown, result = true))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.Shutdown.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.Shutdown, result = false))
        }
    }

    private fun getOnlineUpdates() {
        try {
            logger.info("Command '${UpdateCommand.AvailableOnlineUpdates.name}' will be executed ...")
            sendUpdateResponse(updateInformation = OnlineUpdater.hasUpdates())
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.AvailableOnlineUpdates.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.AvailableOnlineUpdates, result = false))
        }
    }

    private fun manualOnlineUpdate() {
        try {
            logger.info("Command '${UpdateCommand.OnlineUpdate.name}' will be executed ...")
            val response = OnlineUpdater.performUpdate()
            sendUpdateResponse(updateInformation = response)
            // TODO: Restart backend master here ==> check if JSONArray is empty
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.OnlineUpdate.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.OnlineUpdate, result = false))
        }
    }

    private fun manualUpdateViaUsb() {
        try {
            logger.info("Command '${UpdateCommand.UsbUpdate.name}' will be executed ...")
            val response = UsbUpdater.performUpdate()
            sendUpdateResponse(updateInformation = response)
            // TODO: Restart backend master here ==> check if JSONArray is empty
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.UsbUpdate.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.UsbUpdate, result = false))
        }
    }

    private fun enableAutoOnlineUpdate(message: JSONObject) {
        try {
            logger.info("Command '${UpdateCommand.EnableAutoOnlineUpdate.name}' will be executed ...")
            val isEnabled = message.getBoolean(UpdateCommandValues.Enabled.name)
            AutoUpdater.enableOnlineAutoUpdate(isEnabled = isEnabled)
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.EnableAutoOnlineUpdate, result = true))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.EnableAutoOnlineUpdate.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.EnableAutoOnlineUpdate, result = false))
        }
    }

    private fun enableAutoOfflineUpdate(message: JSONObject) {
        try {
            logger.info("Command '${UpdateCommand.EnableAutoOfflineUpdate.name}' will be executed ...")
            val isEnabled = message.getBoolean(UpdateCommandValues.Enabled.name)
            AutoUpdater.enableOfflineAutoUpdate(isEnabled = isEnabled)
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.EnableAutoOfflineUpdate, result = true))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.EnableAutoOfflineUpdate.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.EnableAutoOfflineUpdate, result = false))
        }
    }

    private fun isAutoOnlineUpdateEnabled() {
        try {
            logger.info("Command '${UpdateCommand.IsAutoOnlineUpdateEnabled.name}' will be executed ...")
            val isEnabled = AutoUpdater.isOnlineAutoUpdateEnabled()
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.IsAutoOnlineUpdateEnabled, result = isEnabled))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.IsAutoOnlineUpdateEnabled.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.IsAutoOnlineUpdateEnabled, result = false))
        }
    }

    private fun isAutoOfflineUpdateEnabled() {
        try {
            logger.info("Command '${UpdateCommand.IsAutoOfflineUpdateEnabled.name}' will be executed ...")
            val isEnabled = AutoUpdater.isOnlineAutoUpdateEnabled()
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.IsAutoOfflineUpdateEnabled, result = isEnabled))
        } catch (ex: Exception) {
            logger.error("Error occurred while executing command '${UpdateCommand.IsAutoOfflineUpdateEnabled.name}'!\n${ex.message}")
            sendUpdateResponse(updateInformation = getUpdateResponse(command = UpdateCommand.IsAutoOfflineUpdateEnabled, result = false))
        }
    }

    @Synchronized
    override fun sendUpdateResponse(updateInformation: JSONArray) {
        if (::commandSocket.isInitialized) {
            updateInformation.forEach { information ->
                commandSocket.sendResponseMessage(information = information as JSONObject)
            }
        } else {
            logger.warn("Command socket isn't initialized yet! Cannot send response ...")
        }
    }

    private fun sendUpdateResponse(updateInformation: JSONObject) {
        if (::commandSocket.isInitialized) {
            commandSocket.sendResponseMessage(information = updateInformation)
        } else {
            logger.warn("Command socket isn't initialized yet! Cannot send response ...")
        }
    }

    @Synchronized
    override fun setCommandSocketOnSlaveHandler(commandSocket: CommandSocket) {
        this.commandSocket = commandSocket
    }
}