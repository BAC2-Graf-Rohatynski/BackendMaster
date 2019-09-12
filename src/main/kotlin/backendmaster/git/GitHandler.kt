package backendmaster.git

import backendmaster.git.interfaces.IGitHandler
import backendmaster.network.Connection
import backendmaster.path.MyPath
import enumstorage.update.UpdateCommandValues
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.diff.DiffEntry
import org.eclipse.jgit.lib.Repository
import java.io.File
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.json.JSONObject
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import propertystorage.BranchProperties
import java.lang.Exception

object GitHandler: IGitHandler {
    private val logger: Logger = LoggerFactory.getLogger(GitHandler::class.java)

    init {
        try {
            if (Connection.checkForInternetConnection()) {
                logger.info("Loading Git repository ...")
                loadSingleRepository()
                logger.info("Git repository loaded")
            } else {
                logger.warn("Git repositories cannot be loaded due to no internet connection!")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred during loading Git repository!\n${ex.message}")
        }
    }

    @Synchronized
    override fun pullFirmwares()  {
        return try {
            logger.info("Starting firmware update via Git ...")
            resetHead()
            val git = Git.open(File(MyPath.getLibPath()))

            if (checkIfBranchHasDifferences(git = git)) {
                logger.info("Changes detected. Trying to pull ...")
                val pullResult = git.pull().call()
                logger.info("Pull was ${pullResult.isSuccessful}")
            } else {
                logger.info("No changes detected")
            }
        } catch (ex: Exception) {
            logger.error("Error occurred during pulling firmware\n${ex.message}")
        }
    }

    @Synchronized
    override fun hasUpdates(): JSONObject {
        val git = Git.open(File(MyPath.getLibPath()))
        return JSONObject().put(UpdateCommandValues.Differences.name, !getBranchHasDifferences(git = git).isEmpty())
    }

    private fun resetHead() {
        logger.info("Reset branch ...")
        Git.open(File(MyPath.getLibPath())).reset().setMode(ResetCommand.ResetType.HARD).call()
        logger.info("Branch reset")
    }

    private fun loadSingleRepository(): Repository {
        return if (checkIfRepositoryIsExisting()) {
            openRepository()
        } else {
            logger.warn("Git repo NOT exiting!")
            cloneRepository()
            openRepository()
        }
    }

    private fun openRepository(): Repository {
        logger.info("Opening git repo ...")
        val repository = createRepository()
        logger.info("Git repo loaded")
        return repository
    }

    private fun cloneRepository(): Repository {
        logger.info("Cloning Git rep ...")
        val repository = createRepository()
        Git.cloneRepository().setURI(BranchProperties.getBranchName()).setDirectory(File(MyPath.getLibPath())).call()
        logger.info("Git repo cloned")
        return repository
    }

    private fun createRepository(): Repository = FileRepositoryBuilder.create(File(MyPath.getLibPath()))

    private fun checkIfRepositoryIsExisting(): Boolean = File("${MyPath.getLibPath()}.git").exists()

    private fun checkIfBranchHasDifferences(git: Git): Boolean {
        val diffs: List<DiffEntry> = git.diff().call()

        diffs.forEach { entry ->
            logger.info("Changes: $entry || Old Id: ${entry.oldId} || New Id: ${entry.newId}")
        }

        return diffs.isNotEmpty()
    }

    private fun getBranchHasDifferences(git: Git): List<DiffEntry> = git.diff().call()
}