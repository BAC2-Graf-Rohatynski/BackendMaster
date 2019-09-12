package backendmaster.updater.interfaces

import org.json.JSONArray

interface IUsbUpdater {
    fun performUpdate(): JSONArray
    fun updatesAvailable(path: String? = null): Boolean
}