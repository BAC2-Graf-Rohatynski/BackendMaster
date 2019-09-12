package backendmaster.updater.interfaces

import org.json.JSONObject

interface ISlaveUpdater {
    fun checkForUpdates()
    fun getUpdateInformation(): JSONObject
}