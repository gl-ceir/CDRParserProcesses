package com.glocks.files;

import java.io.File;
import java.time.LocalDateTime;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileList {

    static Logger logger = LogManager.getLogger(FileList.class);

    public ArrayList< String[]> remaingFileList(String reportName, String basePath) {

        // System.out.println("In Remain File List");
        ArrayList< String[]> result = null;
        ArrayList< String[]> fileNames = null;
        String[] fileDetails = null;
        File[] files = null;
        String path = null;
        String fileNameToMatch = reportName;//".csv";
        BigDecimal bd = null;
        //float size = 0;
        //int i         = 0;
        try {
            //path = "/home/pview/pview/upload/"+reportName+"/";
            //path  = "/home/www/html/fact/pview/upload/"+reportName+"/";
            path = basePath;
            files = new File(path).listFiles();
            // System.out.println("File is " + path);
            // System.out.println("File is " + files);
            Collections.sort(Arrays.asList(files));
            result = new ArrayList< String[]>();
            fileNames = new ArrayList< String[]>();
            for (File file : files) {
                // // System.out.println(file.getName());
                // System.out.println("file name is " + file.getName());
//				fileNames.add(fileDetails);

                if ((file.getName().toString().toLowerCase().contains(fileNameToMatch.toLowerCase()))) {
                    bd = new BigDecimal((float) file.length() / 1024 / 1024).setScale(2, RoundingMode.HALF_EVEN);
                    //size = file.length()/1024/1024;
                    fileDetails = new String[]{file.getName(), file.getPath(), String.valueOf(bd)};
                    // // System.out.println("File Name ["+file.getName()+"],file path ["+file.getPath()+"]");
                    fileNames.add(fileDetails);
                    //fileNames[i] = file.getPath();
                    //i++;
                }
            }
            // // System.out.println("Files list size ["+fileNames.size()+"]");
            result = fileNames;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            fileNames = null;
            fileDetails = null;
            files = null;
            path = null;
            fileNameToMatch = null;
            bd = null;
        }
        return result;
    }

    public String readOldestOneFile(String basePath) {
        File oldestFile = null;
        try {

            logger.info(" basePath  :" + basePath);

            File logDir = new File(basePath);
            File[] logFiles = logDir.listFiles();
//          // System.out.println(logFiles.length);
            long oldestDate = Long.MAX_VALUE;
//               File oldestFile = null;
            for (File f : logFiles) {
                if (f.lastModified() < oldestDate) {
                    oldestDate = f.lastModified();
                    oldestFile = f;
                }
            }
            if (oldestFile != null) {
            } else {
                logger.info("No File Found");

            }

        } catch (Exception e) {
            logger.debug("No File Found");
        }
        return oldestFile.getName().toString();
    }

    public ArrayList< String[]> remaingCEIRFileList(String reportName, String filePath) {
        ArrayList< String[]> result = null;
        ArrayList< String[]> fileNames = null;
        String[] fileDetails = null;
        File[] files = null;
        String path = null;
        String fileNameToMatch = "SMART";//".csv";
        BigDecimal bd = null;
        //float size = 0;
        //int i         = 0;
        try {
            //path = "/home/pview/pview/upload/"+reportName+"/";
            //path  = "/home/www/html/fact/pview/upload/"+reportName+"/";
            path = "D:\\lokesh\\CEIR\\" + reportName + "/";
            files = new File(path).listFiles();
            Collections.sort(Arrays.asList(files));
            result = new ArrayList< String[]>();
            fileNames = new ArrayList< String[]>();
            for (File file : files) {
                // // System.out.println(file.getName());
                // System.out.println("file name is " + file.getName());
//				fileNames.add(fileDetails);

                if ((file.getName().toString().toLowerCase().contains(fileNameToMatch.toLowerCase()))) {
                    bd = new BigDecimal((float) file.length() / 1024 / 1024).setScale(2, RoundingMode.HALF_EVEN);
                    //size = file.length()/1024/1024;
                    fileDetails = new String[]{file.getName(), file.getPath(), String.valueOf(bd)};
                    // // System.out.println("File Name ["+file.getName()+"],file path ["+file.getPath()+"]");
                    fileNames.add(fileDetails);
                    //fileNames[i] = file.getPath();
                    //i++;
                }
            }
            // // System.out.println("Files list size ["+fileNames.size()+"]");
            result = fileNames;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            fileNames = null;
            fileDetails = null;
            files = null;
            path = null;
            fileNameToMatch = null;
            bd = null;
        }
        return result;
    }

    public ArrayList< String[]> fileList(String reportName) {
        ArrayList< String[]> result = null;
        ArrayList< String[]> fileNames = null;
        String[] fileDetails = null;
        File[] files = null;
        String path = null;
        String fileNameToMatch = "TGN";//".csv";
        BigDecimal bd = null;
        //float size = 0;
        //int i         = 0;
        try {
            //path = "/home/pview/pview/upload/"+reportName+"/";
            //path  = "/home/www/html/fact/pview/upload/"+reportName+"/";
//			path  = "/home/ildidea/upload/"+reportName+"/";
            path = "D:\\lokesh\\CEIR\\" + reportName + "/";
            files = new File(path).listFiles();
            Collections.sort(Arrays.asList(files));
            result = new ArrayList< String[]>();
            fileNames = new ArrayList< String[]>();
            for (File file : files) {
                // // System.out.println(file.getName());
                if ((file.getName().toString().toLowerCase().contains(fileNameToMatch.toLowerCase()))) {
                    bd = new BigDecimal((float) file.length() / 1024 / 1024).setScale(2, RoundingMode.HALF_EVEN);
                    //size = file.length()/1024/1024;
                    fileDetails = new String[]{file.getName(), file.getPath(), String.valueOf(bd)};
                    // // System.out.println("File Name ["+file.getName()+"],file path ["+file.getPath()+"]");
                    fileNames.add(fileDetails);
                    //fileNames[i] = file.getPath();
                    //i++;
                }
            }
            // // System.out.println("Files list size ["+fileNames.size()+"]");
            result = fileNames;
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            fileNames = null;
            fileDetails = null;
            files = null;
            path = null;
            fileNameToMatch = null;
            bd = null;
        }
        return result;
    }

    public String getFileName(String reportName) {
        String result = null;
        String fileName = null;
        File[] files = null;
        String path = null;
        String fileNameToMatch = "TGN";//".csv";
        try {
            //path  = "/home/www/html/fact/pview/upload/"+reportName+"/";//"/var/www/html/chartnew/upload/";
            path = "/home/ildidea/upload/" + reportName + "/";
            files = new File(path).listFiles();
            for (File file : files) {
                // // System.out.println(file.getName());
                if ((file.getName().toString().toLowerCase().contains(fileNameToMatch.toLowerCase()))) {
                    fileName = file.getName();
                    return fileName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        } finally {
            fileName = null;
            files = null;
            path = null;
            fileNameToMatch = null;
        }
        return result;
    }

    public boolean moveFile(String fileName, String directoryName, String basePath, String type) {
        Logger logger = LogManager.getLogger(FileList.class);

        boolean result = false;
        String path = null;
        File oldFile = null;
        File newFile = null;
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer = null;
        try {
            //path    = "/home/www/html/fact/pview/upload/";
//			path = "/home/ildidea/upload/";
//			String oldpath = "D:\\lokesh\\CEIR\\";
            path = basePath + "/";

            oldFile = new File(path + directoryName + "/" + fileName);
            logger.info("performing moving of file type[" + type + "] base path is " + oldFile);
            // System.out.println("old File name is " + oldFile);
            // System.out.println("Going to move file [" + path + "old/" + fileName + "]");
            if (type.equalsIgnoreCase("error")) {
                newFile = new File(path + "old/" + fileName + type);
            } else {
                newFile = new File(path + "old/" + fileName);
            }
            is = new FileInputStream(oldFile);
            os = new FileOutputStream(newFile);
            buffer = new byte[1024];
            while (is.read(buffer) > 0) {
                os.write(buffer);
            }
            is.close();
            os.close();
            oldFile.deleteOnExit();
            if (oldFile.exists()) {
                // System.out.println("File Available " + oldFile);
            } else {
                // System.out.println("File NOt Available");
            }
            if (oldFile.delete()) {
                // System.out.println("file Deleted Successfully");
            } else {
                // System.out.println("Not Able to delete file");
            }
//			oldFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("GEtting Exception " + e);
            result = false;
        } finally {
            path = null;
            oldFile = null;
            newFile = null;
            is = null;
            os = null;
            buffer = null;
        }
        return result;
    }

    public boolean moveFileFeature(String fileName, String directoryName, String basePath, String type) {
        Logger logger = LogManager.getLogger(FileList.class);

        boolean result = false;
        String path = null;
        File oldFile = null;
        File newFile = null;
        InputStream is = null;
        OutputStream os = null;
        byte[] buffer = null;
        try {
            path = basePath + "/";

            oldFile = new File(path + "/" + fileName);
            logger.info("performing moving of file type[" + type + "] base path is " + oldFile);
            // System.out.println("old File name is " + oldFile);
            // System.out.println("Going to move file [" + path + fileName + "]");
            if (type.equalsIgnoreCase("error")) {
                newFile = new File(path + fileName + directoryName + type);
            } else {
                newFile = new File(path + fileName);
            }
            is = new FileInputStream(oldFile);
            os = new FileOutputStream(newFile);
            buffer = new byte[1024];
            while (is.read(buffer) > 0) {
                os.write(buffer);
            }
            is.close();
            os.close();
            oldFile.deleteOnExit();
            if (oldFile.exists()) {
                // System.out.println("File Available " + oldFile);
            } else {
                // System.out.println("File NOt Available");
            }
            if (oldFile.delete()) {
                // System.out.println("file Deleted Successfully");
            } else {
                // System.out.println("Not Able to delete file");
            }
//			oldFile.delete();
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println("GEtting Exception " + e);
            result = false;
        } finally {
            path = null;
            oldFile = null;
            newFile = null;
            is = null;
            os = null;
            buffer = null;
        }
        return result;
    }

    public void moveCDRFile(Connection conn, String fileName, String opertorName1, String fileFolderPath, String source) {

        String opertorName = opertorName1.toLowerCase();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
        String date = df.format(new Date());

        LocalDateTime myObj = LocalDateTime.now();

        String timeSec = myObj.toString().substring((myObj.toString().length() - 3), (myObj.toString().length()));

        File folder = null;
        String storagePath = new com.glocks.parser.HexFileReader().getFilePath(conn, "CdrProcessedFileStoragePath");  //  with /
        folder = new File(storagePath + opertorName);
        try {
            if (!folder.exists()) {
                folder.mkdir();
            }
            logger.debug("folder Created ::" + folder);
            folder = new File(storagePath + opertorName + "/" + source);
            if (!folder.exists()) {
                folder.mkdir();
            }
            folder = new File(storagePath + opertorName + "/" + source + "/" + date);    //+ "/" + datewithTime
            if (!folder.exists()) {
                folder.mkdir();
            }

            try {
                Path path = Paths.get(folder + "/" + fileName);
                Files.deleteIfExists(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

//        folder = new File(storagePath + opertorName + "/" + source + "/" + date  + "/" + timeSec);    //+ "/" + datewithTime
//            if (!folder.exists()) {
//                folder.mkdir();
//            }
            logger.debug("folder Created ::" + folder);
            logger.info(" File Move From ::" + fileFolderPath + fileName);
//            logger.info(" File Move To ::" + storagePath + opertorName + "/" + source + "/" + date + "/" + timeSec + "/" + fileName);

            Path temp = Files.move(Paths.get(fileFolderPath + fileName),
                    Paths.get(storagePath + opertorName + "/" + source + "/" + date + "/" + fileName));
                    //                    Paths.get(storagePath + opertorName + "/" + source + "/" + date + "/" + timeSec + "/" + fileName));
                    
            if (temp != null) {
                logger.info("File renamed and moved successfully");
            } else {
                logger.warn("Failed to move the file");
            }
        } catch (IOException e) {
            logger.error("Error :" + e);
        }

    }

}
