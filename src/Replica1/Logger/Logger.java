package Replica1.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Krutik Gevariya (40232386)
 * @version 3.0
 */
public class Logger {
    public static final int LOG_TYPE_SERVER = 1;
    public static final int LOG_TYPE_CLIENT = 0;

    public static void clientLog(String clientID, String action, String requestParams, String response) throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " Client Action: " + action + " | RequestParameters: " + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    public static void clientLog(String clientID, String msg) throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(clientID, LOG_TYPE_CLIENT), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void serverLog(String serverID, String clientID, String requestType, String requestParams, String serverResponse) throws IOException {

        if (clientID.equals("null")) {
            clientID = "Event Manager";
        }
        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " ClientID: " + clientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {

        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void deleteALogFile(String ID) throws IOException {

        String fileName = getFileName(ID, LOG_TYPE_CLIENT);
        File file = new File(fileName);
        file.delete();
    }

    private static String getFileName(String ID, int logType) {
        final String dir = System.getProperty("user.dir")+"\\src\\Replica1";
        String fileName = dir;
//        String path = "C:\\Users\\admin\\Desktop\\CU Materials\\Winter 2023 Term\\Distributed_Systems\\Assignment2_Trial\\A2\\src\\Logs";
        if (logType == LOG_TYPE_SERVER) {
            if (ID.equalsIgnoreCase("ATW")) {
                fileName = dir + "\\Logs\\Server\\Atwater.txt";
//                fileName = path + "\\Server\\Atwater.txt";
            } else if (ID.equalsIgnoreCase("VER")) {
                fileName = dir + "\\Logs\\Server\\Verdun.txt";
//                fileName = path + "\\Server\\Verdun.txt";
            } else if (ID.equalsIgnoreCase("OUT")) {
                fileName = dir + "\\Logs\\Server\\Outremont.txt";
//                fileName = path + "\\Server\\Outremont.txt";
            }
        } else {
            fileName = dir + "\\Logs\\Customer\\" + ID + ".txt";
//            fileName = path + "\\Customer\\ " + ID + ".txt";
        }
        return fileName;
    }

    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}
