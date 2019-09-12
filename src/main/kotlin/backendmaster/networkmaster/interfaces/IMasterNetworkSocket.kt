package backendmaster.networkmaster.interfaces

import java.io.File

interface IMasterNetworkSocket {
    fun send(file: File)
    fun closeSockets()
}