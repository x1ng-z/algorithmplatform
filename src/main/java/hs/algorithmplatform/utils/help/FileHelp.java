package hs.algorithmplatform.utils.help;

import hs.algorithmplatform.utils.bridge.ExecutePythonBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/18 13:01
 */
public class FileHelp {
    private static Logger logger = LoggerFactory.getLogger(FileHelp.class);
    public static String pyfilenamepath = System.getProperty("user.dir") + "\\" + "DLLS" + "\\";

    public static void clearInfoForFile(String fileName) {
        File file = new File(pyfilenamepath + fileName);
        FileWriter fileWriter = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fileWriter = new FileWriter(file);
            fileWriter.write("");
            fileWriter.flush();

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                fileWriter.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    public static void updateFile(String fileName, String codecontext) throws IOException {


        File pyfile = new File(pyfilenamepath + fileName);
        PrintWriter newpyfilestream = null;
        try {
            if (pyfile.exists()) {
                clearInfoForFile(fileName);
                newpyfilestream = new PrintWriter(pyfile);
                String s;
                newpyfilestream.write(codecontext);
                newpyfilestream.flush();
            } else {
                FileHelp.creatpyfile(pyfilenamepath + fileName, codecontext);
//                throw new RuntimeException(pyfilenamepath + fileName + " file is alread exit");
            }
        } finally {
            if (newpyfilestream != null) {
                newpyfilestream.close();
            }

        }

    }

    public static String readInfoFromFile(String fileName) {
        File file = new File(pyfilenamepath + fileName);
        if (!file.exists()) {
            return null;
        }
        StringBuilder resultStr = new StringBuilder();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String str = null;
            while (null != (str = bufferedReader.readLine())) {
                resultStr.append(str + "\n");
            }

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return resultStr.toString();
    }


    public static void creatpyfile(String pyfilename, String pytemplete) throws IOException {

        FileWriter fwriter = null;
//        String pythontemplet=System.getProperty("user.dir") + "\\config"+"\\"+pytemplete;
        try {
            fwriter = new FileWriter(pyfilename);
            fwriter.write(pytemplete);
//            comstomizpytempletreader = new BufferedReader(new FileReader(new File(pytemplete.getBytes())));
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("can't find customizepythontemplates" + "the dir is " + pyfilename);
        } finally {
            if (fwriter != null) {
                fwriter.flush();
                fwriter.close();
            }

        }
    }


}
