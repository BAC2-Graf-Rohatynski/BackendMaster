package backendmaster.updater

import backendmaster.handler.CommandHandler
import backendmaster.network.Connection
import backendmaster.updater.interfaces.IAutoUpdater
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.UpdateProperties
import java.lang.Exception

object AutoUpdater: IAutoUpdater {
    private val logger: Logger = LoggerFactory.getLogger(AutoUpdater::class.java)

    @Synchronized
    override fun isEnabled(): Boolean = UpdateProperties.getAutoUpdateOfflineEnabled() || UpdateProperties.getAutoUpdateOnlineEnabled()

    @Synchronized
    override fun checkForUpdates() {
        try {
            logger.info("Checking for firmware updates ...")

            if (UpdateProperties.getAutoUpdateOfflineEnabled()) {
                if (UsbUpdater.updatesAvailable()) {
                    logger.info("Offline update is available!")
                    val response = UsbUpdater.performUpdate()
                    CommandHandler.sendUpdateResponse(updateInformation = response)
                    // TODO: Restart backend master here ==> check if JSONArray is empty
                } else {
                    logger.info("No offline update available!")
                }
            }

            if (UpdateProperties.getAutoUpdateOnlineEnabled()) {
                if (Connection.checkForInternetConnection()) {
                    logger.info("Online update is available!")
                    val response = OnlineUpdater.performUpdate()
                    CommandHandler.sendUpdateResponse(updateInformation = response)
                    // TODO: Restart backend master here ==> check if JSONArray is empty
                } else {
                    logger.info("No online update available!")
                }
            }
        } catch (ex: Exception) {
            logger.error("Error occurred during firmware update!\n${ex.message}")
        }
    }

    @Synchronized
    override fun isOfflineAutoUpdateEnabled(): Boolean = UpdateProperties.getAutoUpdateOfflineEnabled()

    @Synchronized
    override fun isOnlineAutoUpdateEnabled(): Boolean = UpdateProperties.getAutoUpdateOnlineEnabled()

    @Synchronized
    override fun enableOfflineAutoUpdate(isEnabled: Boolean): Any = UpdateProperties.setAutoUpdateOfflineEnabled(enabled = isEnabled)

    @Synchronized
    override fun enableOnlineAutoUpdate(isEnabled: Boolean): Any = UpdateProperties.setAutoUpdateOnlineEnabled(enabled = isEnabled)
}