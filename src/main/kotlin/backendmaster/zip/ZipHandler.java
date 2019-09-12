package backendmaster.zip;

import backendmaster.zip.interfaces.IZipHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Alternative: zip4j
 */
public class ZipHandler implements IZipHandler {
    private Logger logger = LoggerFactory.getLogger("ZipHandler");

    public void unzip(String pathToZipFile) {
        File zipFile = new File(pathToZipFile);
        String targetPath = zipFile.getAbsolutePath().replace(".zip", "");
        byte[] buffer = new byte[1024];

        try{
            ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry = zipInputStream.getNextEntry();

            while (zipEntry != null){
                String fileName = zipEntry.getName();

                if (!checkIfFolder(fileName)) {
                    logger.info("Folder to unzip: "+ fileName);
                    new File(targetPath + "\\" + fileName).mkdirs();
                } else {
                    new File(targetPath).mkdirs();
                    File newFile = new File(targetPath + File.separator + fileName);
                    logger.info("File to unzip: "+ newFile.getAbsoluteFile());
                    FileOutputStream fileOutputStream = new FileOutputStream(newFile);
                    int length;

                    while ((length = zipInputStream.read(buffer)) > 0) {
                        fileOutputStream.write(buffer, 0, length);
                    }

                    fileOutputStream.close();
                }

                zipEntry = zipInputStream.getNextEntry();
            }

            zipInputStream.closeEntry();
            zipInputStream.close();
            logger.info("Unzipping finished");

        } catch(Exception ex) {
            logger.error("Error occurred during unzipping update package!\n" + ex.getMessage());
        }
    }

    private Boolean checkIfFolder(String fileName) { return fileName.contains("."); }
}
