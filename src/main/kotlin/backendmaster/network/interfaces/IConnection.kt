package backendmaster.network.interfaces

interface IConnection {
    fun checkForInternetConnection(): Boolean
}