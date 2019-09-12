package backendmaster.command

import apibuilder.json.Json
import backendmaster.BackendMasterRunner
import backendmaster.command.interfaces.ICommandSocket
import backendmaster.handler.CommandHandler
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.lang.Exception

class CommandSocket(private val clientSocket: Socket): Thread(), ICommandSocket {
    private lateinit var printWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private val logger: Logger = LoggerFactory.getLogger(CommandSocket::class.java)

    override fun run() {
        try {
            setCommandSocketOnSlaveHandler()
            openSockets()
            receive()
        } catch (ex: Exception) {
            logger.error("Error occurred while running the socket!\n${ex.message}")
        } finally {
            closeSockets()
        }
    }

    private fun setCommandSocketOnSlaveHandler() = CommandHandler.setCommandSocketOnSlaveHandler(commandSocket = this)

    @Synchronized
    override fun sendResponseMessage(information: JSONObject) {
        try {
            if (::printWriter.isInitialized) {
                val messageToSend = Json.replaceUnwantedChars(jsonObject = information)
                printWriter.println(messageToSend)
                logger.info("Message '$messageToSend' sent")
            } else {
                throw Exception("Print writer isn't initialized yet!")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred during sending message: ${ex.message}")
        }
    }

    private fun receive() {
        bufferedReader.use {
            while (BackendMasterRunner.isRunnable()) {
                try {
                    val inputLine = bufferedReader.readLine()

                    if (inputLine != null) {
                        val message = JSONObject(inputLine)
                        logger.info("Message '$message' received")
                        CommandHandler.parse(message = message)
                    }
                } catch (ex: Exception) {
                    logger.error("Error occurred while parsing message!\n${ex.message}")
                }
            }
        }
    }

    private fun openSockets() {
        logger.info("Opening sockets ...")
        printWriter = PrintWriter(clientSocket.getOutputStream(), true)
        bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        logger.info("Sockets opened")
    }

    @Synchronized
    override fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::printWriter.isInitialized) {
                printWriter.close()
            }

            if (::bufferedReader.isInitialized) {
                bufferedReader.close()
            }

            clientSocket.close()
            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}