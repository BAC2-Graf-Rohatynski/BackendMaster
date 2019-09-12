package backendmaster.networkmaster

import backendmaster.networkmaster.interfaces.IMasterNetworkSocketHandler
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object MasterNetworkSocketHandler: IMasterNetworkSocketHandler {
    private var networkSocket: MasterNetworkSocket? = null
    private val logger: Logger = LoggerFactory.getLogger(MasterNetworkSocketHandler::class.java)

    init {
        connect()
    }

    @Synchronized
    override fun sendUpdateFile(file: ByteArray) {
        try {
            networkSocket?.send(file = file) ?: logger.warn("Socket hasn't been connected yet!")
        } catch (ex: Exception) {
            logger.error("Error while waiting for response!\n${ex.message}")
        }
    }

    @Synchronized
    override fun closeSockets() {
        networkSocket?.closeSockets() ?: return logger.warn("Socket hasn't been connected yet!")
        networkSocket = null
    }

    private fun connect() {
        try {
            logger.info("Connecting ...")
            networkSocket = MasterNetworkSocket()
            logger.info("Connected")
        } catch (ex: Exception) {
            logger.error("Error occurred while connecting!\n${ex.message}")
        }
    }
}