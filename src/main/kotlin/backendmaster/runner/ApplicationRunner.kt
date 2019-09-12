package backendmaster.runner

import backendmaster.runner.interfaces.IApplicationRunner
import databasemanager.DatabaseManagerRunner
import errormanager.ErrorManagerRunner
import licensemanager.LicenseManagerRunner
import masternetworkmanager.MasterNetworkManagerRunner
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import porthandler.PortHandlerRunner
import synchmanager.SynchManagerRunner
import java.lang.Exception

object ApplicationRunner: IApplicationRunner {
    private val logger: Logger = LoggerFactory.getLogger(ApplicationRunner::class.java)

    @Synchronized
    override fun stopAllApplications() {
        try {
            logger.info("Stopping all applications ...")
            ErrorManagerRunner.stop()
            SynchManagerRunner.stop()
            DatabaseManagerRunner.stop()
            LicenseManagerRunner.stop()
            MasterNetworkManagerRunner.stop()
            PortHandlerRunner.stop()
            logger.info("All applications stopped")
        } catch (ex: Exception) {
            logger.error("Error occurred during stopping all applications!\n${ex.message}")
        }
    }

    @Synchronized
    override fun startAllApplications() {
        try {
            logger.info("Starting all applications ...")
            ErrorManagerRunner.start()
            SynchManagerRunner.start()
            DatabaseManagerRunner.start()
            LicenseManagerRunner.start()
            MasterNetworkManagerRunner.start()
            PortHandlerRunner.start()
            logger.info("All applications started")
        } catch (ex: Exception) {
            logger.error("Error occurred during starting all applications!\n${ex.message}")
        }
    }
}