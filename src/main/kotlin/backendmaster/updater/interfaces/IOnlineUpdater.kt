package backendmaster.updater.interfaces

import org.json.JSONArray
import org.json.JSONObject

interface IOnlineUpdater {
    fun performUpdate(): JSONArray
    fun hasUpdates(): JSONObject
}