package backendmaster.network

import backendmaster.network.interfaces.IConnection
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

object Connection: IConnection {
    private val logger: Logger = LoggerFactory.getLogger(Connection::class.java)

    @Synchronized
    override fun checkForInternetConnection(): Boolean {
        return try {
            logger.info("Checking for Internet connection ...")
            val connection = URL("http://www.google.com").openConnection()
            connection.connect()
            connection.getInputStream().close()
            logger.info("Internet connection available")
            true
        } catch (ex: Exception) {
            logger.warn("Internet connection NOT available!")
            false
        }
    }
}