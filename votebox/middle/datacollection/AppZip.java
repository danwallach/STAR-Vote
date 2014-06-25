package votebox.middle.datacollection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


/**
 * Stolen from http://www.mkyong.com/java/how-to-compress-files-in-zip-format/
 */
/* TODO file path construction */
public class AppZip
{
    List<String> fileList;
    private String OUTPUT_ZIP_FILE = "C:\\MyFile.zip";
    private String SOURCE_FOLDER = "C:\\testzip";

    public AppZip(File source){
        SOURCE_FOLDER = source.getAbsolutePath();
        fileList = new ArrayList<>();
    }

    /**
     * Zip it
     * @param zipFile output ZIP file location
     */
    public void zipIt(String zipFile){

        byte[] buffer = new byte[1024];

        try {

            FileOutputStream fos    = new FileOutputStream(zipFile);
            ZipOutputStream zos     = new ZipOutputStream(fos);

            /* For each file in the list, pull it out */
            for (String file : this.fileList) {

                ZipEntry ze = new ZipEntry(file);
                zos.putNextEntry(ze);

                /* Set up a new file stream for the file */
                FileInputStream in = new FileInputStream(SOURCE_FOLDER + File.separator + file);

                int len;

                /* Write to the file */
                while ((len = in.read(buffer)) > 0)
                    zos.write(buffer, 0, len);

                in.close();
            }

            zos.closeEntry();

            /* Remember to close it */
            zos.close();

        }
        catch (IOException ex) { ex.printStackTrace(); }
    }

    /**
     * Traverse a directory and get all files,
     * and add the file into fileList
     * @param node file or directory
     */
    public void generateFileList(File node){

        /* Add file only */
        if(node.isFile())
            fileList.add(generateZipEntry(node.getAbsoluteFile().toString()));

        if(node.isDirectory()){

            String[] subNote = node.list();

            for(String filename : subNote)
                generateFileList(new File(node, filename));
        }

    }

    /**
     * Format the file path for zip
     * @param file  file path
     * @return      Formatted file path
     */
    private String generateZipEntry(String file){
        return file.substring(SOURCE_FOLDER.length()+1, file.length());
    }
}