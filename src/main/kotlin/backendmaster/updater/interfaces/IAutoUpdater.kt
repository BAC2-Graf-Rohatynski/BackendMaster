package backendmaster.updater.interfaces

interface IAutoUpdater {
    fun isEnabled(): Boolean
    fun checkForUpdates()
    fun enableOfflineAutoUpdate(isEnabled: Boolean): Any
    fun enableOnlineAutoUpdate(isEnabled: Boolean): Any
    fun isOfflineAutoUpdateEnabled(): Boolean
    fun isOnlineAutoUpdateEnabled(): Boolean
}