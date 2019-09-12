package backendmaster.handler.interfaces

import backendmaster.command.CommandSocket
import org.json.JSONArray
import org.json.JSONObject

interface ICommandHandler {
    fun parse(message: JSONObject)
    fun sendUpdateResponse(updateInformation: JSONArray)
    fun setCommandSocketOnSlaveHandler(commandSocket: CommandSocket)
}