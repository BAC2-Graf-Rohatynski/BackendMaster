package backendmaster.networkmaster

import apibuilder.backendmaster.BackendApi
import backendmaster.BackendMasterRunner
import backendmaster.networkmaster.interfaces.IMasterNetworkSocket
import enumstorage.api.ApiValue
import enumstorage.stage.StageCommand
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.PortProperties
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.lang.Exception
import kotlin.concurrent.thread

class MasterNetworkSocket: IMasterNetworkSocket {
    private lateinit var clientSocket: Socket
    private lateinit var printWriter: PrintWriter
    private lateinit var bufferedReader: BufferedReader
    private var run = true
    private val logger: Logger = LoggerFactory.getLogger(MasterNetworkSocket::class.java)

    init {
        try {
            openSockets()
            receive()
        } catch (ex: Exception) {
            logger.error("Error socket failure while running socket!\n${ex.message}")
            MasterNetworkSocketHandler.closeSockets()
        }
    }

    private fun openSockets() {
        logger.info("Opening sockets ...")
        clientSocket = Socket("127.0.0.1", PortProperties.getNetworkPort())
        printWriter = PrintWriter(clientSocket.getOutputStream(), true)
        bufferedReader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
        logger.info("Sockets opened")
    }

    @Synchronized
    override fun send(file: File) {
        try {
            if (::printWriter.isInitialized) {
                logger.info("Sending slave update file to network master ...")
                printWriter.println(BackendApi.getSlaveSoftwareUpdateMessage(file = file))
                logger.info("Sending slave update file sent")
            } else {
                throw Exception("Print writer isn't initialized yet!")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred while sending message!\n${ex.message}")
        }
    }

    private fun receive() {
        logger.info("Hearing for messages ...")

        thread {
            bufferedReader.use {
                while (run && BackendMasterRunner.isRunnable()) {
                    try {
                        val inputLine = bufferedReader.readLine()

                        if (inputLine != null) {
                            val message = JSONObject(inputLine)
                            logger.info("Message '$message' received")

                            if (message.has(ApiValue.Command.name)) {
                                if (message.getString(ApiValue.Command.name) == StageCommand.SlaveUpdate.name) {
                                    logger.info("Slave update has been received by master network manager\n$message")
                                    run = false
                                    MasterNetworkSocketHandler.closeSockets()
                                }
                            }
                        }
                    } catch (ex: Exception) {
                        logger.error("Error occurred while parsing message!\n${ex.message}")
                    }
                }
            }
        }
    }

    @Synchronized
    override fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::printWriter.isInitialized) {
                printWriter.close()
            }

            if (::clientSocket.isInitialized) {
                clientSocket.close()
            }

            if (::bufferedReader.isInitialized) {
                bufferedReader.close()
            }

            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}