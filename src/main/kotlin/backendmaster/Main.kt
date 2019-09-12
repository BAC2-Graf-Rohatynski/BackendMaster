package backendmaster

import org.apache.log4j.BasicConfigurator

fun main(args: Array<String>) {
    BasicConfigurator.configure()
    BackendMasterRunner.start()

    /**
     * TODO:
     * -> Java Wrapper Script to start backend master -> start the newest backend master version and delete all others
     * -> Backend updater -> load into same folder as the current jar -> see TODO's
     * -> Slave updater
     * -> GIT doku für Roßi's Dad -> Git Repo für Slave anlegen
     */
}
