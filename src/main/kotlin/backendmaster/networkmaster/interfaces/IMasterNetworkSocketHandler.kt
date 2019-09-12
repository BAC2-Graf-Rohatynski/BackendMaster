package backendmaster.networkmaster.interfaces

import java.io.File

interface IMasterNetworkSocketHandler {
    fun sendUpdateFile(file: File)
    fun closeSockets()
}