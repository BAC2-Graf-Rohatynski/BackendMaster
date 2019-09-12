package backendmaster.path

import backendmaster.path.interfaces.IMyPath

object MyPath: IMyPath {
    @Synchronized
    override fun getProjectPath(): String = System.getProperty("user.dir")

    @Synchronized
    override fun getLibPath(): String = "${getProjectPath()}\\libs\\"
}