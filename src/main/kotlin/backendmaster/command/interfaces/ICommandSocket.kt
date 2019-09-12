package backendmaster.command.interfaces

import org.json.JSONObject

interface ICommandSocket {
    fun sendResponseMessage(information: JSONObject)
    fun closeSockets()
}