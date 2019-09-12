package backendmaster.git.interfaces

import org.json.JSONObject

interface IGitHandler {
    fun pullFirmwares()
    fun hasUpdates(): JSONObject
}