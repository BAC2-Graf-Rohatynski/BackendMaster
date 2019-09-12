package backendmaster.command

import backendmaster.BackendMasterRunner
import backendmaster.command.interfaces.ICommandSocketHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.PortProperties
import java.net.ServerSocket
import kotlin.concurrent.thread

object CommandSocketHandler: ICommandSocketHandler {
    private lateinit var serverSocket: ServerSocket
    private lateinit var commandSocket: CommandSocket
    private val port: Int = PortProperties.getBackendPort()
    private val logger: Logger = LoggerFactory.getLogger(CommandSocketHandler::class.java)

    init {
        thread {
            try {
                openSocket()
                acceptClients()
            } catch (ex: Exception) {
                logger.error("Error occurred while running socket handler!\n${ex.message}")
            } finally {
                closeSockets()
            }
        }
    }

    private fun acceptClients() {
        while (BackendMasterRunner.isRunnable()) {
            try {
                logger.info("Waiting for client ...")
                commandSocket = CommandSocket(clientSocket = serverSocket.accept())
                commandSocket.start()
                logger.info("Client added")
            } catch (ex: Exception) {
                logger.error("Error occurred while waiting for new clients!\n${ex.message}")
            }
        }
    }

    private fun openSocket() {
        logger.info("Opening command socket on port '$port'")
        serverSocket = ServerSocket(port)
        logger.info("Command socket opened")
    }

    @Synchronized
    override fun closeSockets() {
        try {
            logger.info("Closing sockets ...")

            if (::serverSocket.isInitialized) {
                serverSocket.close()
            }

            if (::commandSocket.isInitialized) {
                commandSocket.closeSockets()
            }

            logger.info("Sockets closed")
        } catch (ex: Exception) {
            logger.error("Error occurred while closing sockets!\n${ex.message}")
        }
    }
}