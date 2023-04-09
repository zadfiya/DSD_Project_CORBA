package Client.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientLogger {
    public static final int LOG_TYPE_CLIENT = 0;

    public static void clientLog(String clientID, String action, String requestParams, String response) throws IOException {

        File file = new File(getFileName(clientID, LOG_TYPE_CLIENT));
        FileWriter fileWriter;
        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), true);
        }
        else
        {

            fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), false);
        }
        //FileWriter fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " Client Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    /**
     *
     * @param clientID
     * @param msg
     * @throws IOException
     */
    public static void clientLog(String clientID, String msg) throws IOException {
        File file = new File(getFileName(clientID, LOG_TYPE_CLIENT));
        FileWriter fileWriter;


        if(file.exists())
        {
            fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), true);
        }
        else
        {
            fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), false);
        }
//        if(file.exists())
//        {
//             fileWriter = new FileWriter(getFileName(clientID, Constant.LOG_TYPE_CLIENT), true);
//        }
//        else
//        {
//            file.mkdirs();
//            file.createTempFile(clientID,".txt");
//            System.out.println(getFileName(clientID, Constant.LOG_TYPE_CLIENT));
//            System.out.println(file.createNewFile());
//            fileWriter = new FileWriter(file, false);
//        }

        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void deleteALogFile(String ID) throws IOException {

        String fileName = getFileName(ID, LOG_TYPE_CLIENT);
        File file = new File(fileName);
        file.delete();
    }

    /**
     *
     * @param ID
     * @param logType
     * @return
     */
    private static String getFileName(String ID, int logType) {
        final String dir = System.getProperty("user.dir");
        String fileName = dir;
        String path="E:\\Concordia\\Winter 2023\\DSD\\Assignments\\Assignment 1\\DSD_40232646\\src\\Logs";

            //fileName=path+"\\Client\\" + ID + ".txt";
            fileName = dir + "\\Client\\Log\\" + ID + ".txt";

        return fileName;
    }

    /**
     *
     * @return
     */
    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}


