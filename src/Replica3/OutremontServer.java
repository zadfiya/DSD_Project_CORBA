package Replica3;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import Replica3.DMTBSApp.DMTBS;
import Replica3.DMTBSApp.DMTBSHelper;
import Replica3.DMTBSApp.DMTBSPOA;

/**
 * @author Nayankumar
 *
 */

class OUTImpl extends DMTBSPOA {

    private ORB orb;
    public static HashMap<String, Map<String, List<String>>> app_data = new HashMap<String, Map<String, List<String>>>();
    private static int atw_port = 7981;
    private static int ver_port = 4651;
    private static int out_port = 6321;

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    // Admin Methods
    @Override
    public String addMovieSlots(String movieID, String movieName, String[] bookingCapacity) {
        synchronized (this) {
            List<String> caplist = new ArrayList<String>();
            caplist.add(bookingCapacity[0]);
            List<String> cap = new ArrayList<String>();
            Map<String, List<String>> temp = new HashMap<String, List<String>>();
            String result;
            String status;

            String movieDate = movieID.substring(4); // after part
            String day_str = movieDate.substring(0, 2); // day
            int day = Integer.parseInt(day_str);
            String month_str = movieDate.substring(2, 4); // month
            int month = Integer.parseInt(month_str);
            String year_str = movieDate.substring(4); // year
            int year = Integer.parseInt(year_str);

            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 7);
            Date newd = cal.getTime();
            SimpleDateFormat sdformat = new SimpleDateFormat("yyyy-MM-dd");
            Date d1 = new Date();
            try {
                Date d2 = sdformat.parse("20" + year + "-" + month + "-" + day);
                int rst = d1.compareTo(d2);
                int rst2 = d2.compareTo(newd);
                if (rst >= 0 || rst2 >= 0) {
                    result = "You have exceed your week limit for adding the movie";
                    status = "Failed";
                } else {
                    if (app_data.containsKey(movieName)) { // check same movieName exist or not
                        temp = app_data.get(movieName);
                        cap = caplist;

                        if (temp.containsKey(movieID)) { // check same movieID exist or not, if already exist then
                                                         // update
                                                         // booking capacity
                            result = "It already contains the Movie. booking capacity updated";
                            // status = "Success";
                        } else { // if movieId does not exist then add the Movie.
                            // temp.put(movieID, bookingCapacity);
                            result = "Movie added Successfully.";
                        }
                        temp.put(movieID, cap);
                        status = "Success";
                    } else { // if movieName does not exist then add everything for the Movie.
                        cap = caplist;
                        temp.put(movieID, cap);
                        app_data.put(movieName, temp);
                        result = "New Movie added Successfully";
                        status = "Success";
                    }
                }
            } catch (ParseException p) {
                result = "You have exceed your week limit for booking the movie";
                status = "Failed";
                System.out.println(p);
            }
            // Server Log
            try {
                serverLog("Admin", "Add Movie", movieID + "-" + movieName + "-" + bookingCapacity[0], status, result);
            } catch (IOException e) {
                System.out.println(e);
            }
            return result;
        }
    }

    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        String result = "";
        String status = "";
        if (app_data.containsKey(movieName)) {
            Map<String, List<String>> temp = app_data.get(movieName);
            if (temp.containsKey(movieID)) {
                List<String> cap_data = temp.get(movieID);
                // check the user booked the movie or not
                if (cap_data.size() > 1) { // there are user reschedule the booking for them then cancel the movie slot;
                    String nextMovieSlot = nextAvailbaleMovieSlot(movieID, movieName);
                    addMovieSlots(nextMovieSlot, movieName, cap_data.toArray(new String[cap_data.size()]));
                    temp.remove(movieID, cap_data);
                    result = "The " + movieID + " has been removed from and users are rescheduled on "
                            + nextMovieSlot + " for " + movieName;
                    status = "Success";
                } else {
                    temp.remove(movieID, cap_data);
                    result = "The " + movieID + " has been removed from " + movieName;
                    status = "Success";
                }
            } else {
                result = "Error : This Movie ID does not Exist";
                status = "Failed";
            }
        } else {
            result = "Error : This Movie Name does not Exist";
            status = "Failed";
        }
        // Server Log
        try {
            serverLog("Admin", "Remove Movie slot", movieID + "-" + movieName, status, result);
        } catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    public static String nextAvailbaleMovieSlot(String movieID, String movieName) {
        String newDate_before = movieID.substring(0, 4); // before part
        String movieDate = movieID.substring(4); // after part
        String day_str = movieDate.substring(0, 2); // day
        int day = Integer.parseInt(day_str);
        String month_str = movieDate.substring(2, 4); // month
        int month = Integer.parseInt(month_str);
        String year_str = movieDate.substring(4); // year
        int year = Integer.parseInt(year_str);
        if (day >= 28) {
            day = 1;
            day_str = "01";
            if (month < 12) {
                month++;
                if (month < 10) {
                    month_str = "0" + Integer.toString(month);
                } else {
                    month_str = Integer.toString(month);
                }
            } else {
                month = 1;
                month_str = "01";
                year++;
                if (year < 10) {
                    year_str = "0" + Integer.toString(year);
                } else {
                    year_str = Integer.toString(year);
                }
            }
        } else {
            day++;
            if (day < 10) {
                day_str = "0" + Integer.toString(day);
            } else {
                day_str = Integer.toString(day);
            }
        }
        String newDate_after = day_str + month_str + year_str; // new date
        String newBooking = newDate_before + newDate_after; // before + new date part makes new movie slot
        Map<String, List<String>> temp = app_data.get(movieName);
        if (temp.containsKey(newBooking)) { // check slot is available
            newBooking = nextAvailbaleMovieSlot(newBooking, movieName);
        }
        return newBooking;
    }

    @Override
    public String[] listMovieShowsAvailability(String movieName) {
        List<String> result = new ArrayList<String>();
        List<String> params = new ArrayList<String>();
        params.add(movieName);

        result = this.listMovieAvailabilitySupport(movieName);
        String verdun_res = requestAnotherServer("list_movieslots", params, ver_port);
        String atwater_res = requestAnotherServer("list_movieslots", params, atw_port);
        List<String> ver_list = Stream.of(verdun_res.split(",", -1)).collect(Collectors.toList());
        List<String> atw_list = Stream.of(atwater_res.split(",", -1)).collect(Collectors.toList());
        if (ver_list.size() > 1) {
            result.addAll(ver_list);
        }
        if (atw_list.size() > 1) {
            result.addAll(atw_list);
        }
        // Server Log
        try {
            serverLog("Admin", "List Movie availability", movieName, "Success", String.join(", ", result));
        } catch (IOException e) {
            System.out.println(e);
        }
        return result.toArray(new String[result.size()]);
    }

    public String requestAnotherServer(String function, List params, int port) {
        String result = "";
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket();
            InetAddress ip = InetAddress.getLocalHost();
            byte buf[] = null;
            List<String> param_list = params;
            String sending_params = String.join(",", param_list);
            String sending_str = function + "@" + sending_params;
            // System.out.println(sending_str);
            // //<----------------------------------------------------------------------------------------------testing
            buf = sending_str.getBytes();
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, ip, port);
            ds.send(DpSend);
            /**
             * 
             */
            byte[] received = new byte[5000];
            DatagramPacket DpReceive = new DatagramPacket(received, received.length);
            ds.receive(DpReceive);
            result = new String(DpReceive.getData());
            String[] temp = result.split("@");
            result = temp[0];
            // System.out.println(result);
            // //<----------------------------------------------------------------------------------------------testing
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (ds != null) {
                ds.close();
            }
        }
        return result;
    }

    public List listMovieAvailabilitySupport(String movieName) {
        List<String> result = new ArrayList<String>();
        if (app_data.containsKey(movieName)) {
            Map<String, List<String>> temp = app_data.get(movieName);
            for (Map.Entry mapSubElement : temp.entrySet()) {
                String movieID = (String) mapSubElement.getKey();
                List<String> cap_data = (List<String>) mapSubElement.getValue();
                String avail_data = movieID + " " + cap_data.get(0);
                result.add(avail_data);
            }
        }
        return result;
    }

    public void serverLog(String user, String requestType, String params, String status, String result)
            throws IOException {
        final String log_dir = System.getProperty("user.dir"); // get the directory
        String log_file = log_dir;
        // System.out.println(log_dir);
        log_file = log_dir + "\\src\\Replica3\\log\\server\\outremont_logs.txt"; // log file path
        File file = new File(log_file);
        file.createNewFile(); // create a log file if not exist..
        FileWriter fw = new FileWriter(log_file, true);
        PrintWriter pw = new PrintWriter(fw);
        Date logDate = new Date();
        String fmt_str = "yyyy-MM-dd hh:mm:ss a"; // specific date format to add in log file
        DateFormat df = new SimpleDateFormat(fmt_str);
        String log_date = df.format(logDate);
        pw.println(log_date + " " + user + " | " + requestType + " | " + params + " | " + status + " | " + result);
        System.out.println(
                log_date + " " + user + " | " + requestType + " | " + params + " | " + status + " | " + result);
        pw.close(); // close and save the resources.
    }

    // Customer Methods
    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        System.out.println("Book Movie - " + customerID + " " + movieID + " " + movieName);
        Map<String, List<String>> temp = new HashMap<String, List<String>>();
        List<String> cap = new ArrayList<String>();
        List<String> params = new ArrayList<String>();
        params.add(customerID);
        params.add(movieID);
        params.add(movieName);
        params.add(String.valueOf(numberOfTickets));
        String result = "";
        String status = "";
        boolean flag = true;
        if ((movieID.substring(0, 3)).equals("OUT")) {
            if (canBookMovieToday(customerID, movieID, movieName)) {
                if (movieID.substring(0, 3).equals(customerID.substring(0, 3))) {
                    flag = true;
                } else {
                    flag = canbookthisweek(customerID, movieID, movieName);
                }
                if (flag) {
                    if (app_data.containsKey(movieName)) { // check if Movie is exist or not
                        temp = app_data.get(movieName);
                        if (temp.containsKey(movieID)) { // Check is Movie is available on that day
                            cap = temp.get(movieID);
                            int capacity = Integer.parseInt(cap.get(0));
                            if (capacity != 0) { // check Movie slots are full or not.

                                if (capacity >= numberOfTickets) {
                                    capacity = capacity - numberOfTickets; // if Movie slots is available then decrease
                                                                           // the capacity.
                                    result = "Movie booked Successfully";
                                    cap.set(0, Integer.toString(capacity));
                                    while (numberOfTickets > 0) {
                                        cap.add(customerID);
                                        numberOfTickets--;
                                    }
                                    status = "Success";
                                } else {
                                    result = "Movie slot is not enough for you request";
                                    status = "Failed";

                                }
                            } else {
                                result = "All the Movies are booked";
                                status = "Failed";
                            }
                        } else {
                            result = "Movie slot is not available on this day";
                            status = "Failed";
                        }
                    } else {
                        result = "There are no Movie slot for " + movieName;
                        status = "Failed";
                    }
                } else {
                    result = "You have exceed your week limit for booking the movie";
                    status = "Failed";
                }

            } else {
                result = "You cannot further book the Movies for the " + movieName;
                status = "Failed";
            }
        } else if ((movieID.substring(0, 3)).equals("VER")) {
            result = requestAnotherServer("book_movie", params, ver_port);
            if (result.equals("Movie booked Successfully"))
                status = "Success";
            else
                status = "Failed";
        } else if ((movieID.substring(0, 3)).equals("ATW")) {
            result = requestAnotherServer("book_movie", params, atw_port);
            if (result.equals("Movie booked Successfully"))
                status = "Success";
            else
                status = "Failed";
        }
        // Server Log
        try {
            serverLog("Customer " + customerID, "Book a movie", customerID + " " + movieID + " " + movieName, status,
                    result);
        } catch (IOException e) {
            System.out.println(e);
        }
        System.out.println(result);
        return result;
    }

    public boolean canbookthisweek(String customerID, String movieID, String movieName) {
        int count = 0;
        Map<String, List<String>> temp = new HashMap<String, List<String>>();
        List<String> weekdays = new ArrayList<String>();
        weekdays.add(movieID);
        String one = "";
        String two = "";
        if (movieID.substring(3, 4).equals("M")) {
            one = "A";
            two = "E";
        } else if (movieID.substring(3, 4).equals("A")) {
            one = "M";
            two = "E";
        } else if (movieID.substring(3, 4).equals("E")) {
            one = "M";
            two = "A";
        }
        String date = movieID.substring(4);
        String day_one = movieID.substring(0, 3) + one + date;
        String day_two = movieID.substring(0, 3) + two + date;
        weekdays.add(day_one);
        weekdays.add(day_two);
        String day = date.substring(0, 2);
        String month = date.substring(2, 4);
        String year = date.substring(4, 6);
        Calendar calndr1 = Calendar.getInstance();
        calndr1.set(Integer.parseInt(year) + 2000, Integer.parseInt(month) - 1, Integer.parseInt(day));
        Date dt = calndr1.getTime();
        int day_of_week = calndr1.get(Calendar.DAY_OF_WEEK);
        for (int i = 1; i < day_of_week; i++) {
            Calendar cala = Calendar.getInstance();
            cala.setTime(dt);
            cala.add(Calendar.DATE, -i);
            Date dat = cala.getTime();
            DateFormat df = new SimpleDateFormat("ddMMyy");
            String week_day = movieID.substring(0, 3) + "M" + df.format(dat);
            String week_day1 = movieID.substring(0, 3) + "A" + df.format(dat);
            String week_day2 = movieID.substring(0, 3) + "E" + df.format(dat);
            weekdays.add(week_day);
            weekdays.add(week_day1);
            weekdays.add(week_day2);
        }
        for (int i = 1; i <= 7 - day_of_week; i++) {
            Calendar cala2 = Calendar.getInstance();
            cala2.setTime(dt);
            cala2.add(Calendar.DATE, i);
            Date data = cala2.getTime();
            DateFormat df = new SimpleDateFormat("ddMMyy");
            String week_day = movieID.substring(0, 3) + "M" + df.format(data);
            String week_day1 = movieID.substring(0, 3) + "A" + df.format(data);
            String week_day2 = movieID.substring(0, 3) + "E" + df.format(data);
            weekdays.add(week_day);
            weekdays.add(week_day1);
            weekdays.add(week_day2);
        }
        List<String> cap = new ArrayList<String>();
        if (app_data.containsKey(movieName)) {
            temp = app_data.get(movieName);
            for (int i = 0; i < weekdays.size(); i++) {
                if (temp.containsKey(weekdays.get(i))) {
                    cap = temp.get(weekdays.get(i));
                    if (cap.contains(customerID)) {
                        count++;
                    }
                }
            }
        }
        // System.out.println(count);
        if (count < 3)
            return true;
        else
            return false;
    }

    public boolean canBookMovieToday(String customerID, String movieID, String movieName) {
        Map<String, List<String>> temp = new HashMap<String, List<String>>();
        List<String> cap = new ArrayList<String>();
        String morning = "";
        String evening = "";
        String afternoon = "";
        String before = movieID.substring(0, 3);
        String after = movieID.substring(4);
        if (movieID.charAt(3) == 'M') {
            morning = movieID;
            afternoon = before + "A" + after;
            evening = before + "E" + after;
        } else if (movieID.charAt(3) == 'A') {
            morning = before + "M" + after;
            afternoon = movieID;
            evening = before + "E" + after;
        } else if (movieID.charAt(3) == 'E') {
            morning = before + "M" + after;
            afternoon = before + "A" + after;
            evening = movieID;
        }
        if (app_data.containsKey(movieName)) {
            temp = app_data.get(movieName);
            if (temp.containsKey(morning)) {
                cap = temp.get(morning);
                if (cap.contains(customerID)) {
                    return false;
                }
            } else if (temp.containsKey(afternoon)) {
                cap = temp.get(afternoon);
                if (cap.contains(customerID)) {
                    return false;
                }
            } else if (temp.containsKey(evening)) {
                cap = temp.get(evening);
                if (cap.contains(customerID)) {
                    return false;
                }
            }

        }
        return true;
    }

    @Override
    public String[] getBookingSchedule(String customerID) {
        List<String> params = new ArrayList<>();
        params.add(customerID);
        List<String> result = new ArrayList<String>();
        for (Map.Entry mapElement : app_data.entrySet()) {
            String movieName = (String) mapElement.getKey();
            Map<String, List<String>> temp = (Map<String, List<String>>) mapElement.getValue();
            // System.out.println(movieName);
            // System.out.println(temp);
            // result.add(movieName);
            for (Map.Entry mapSubElement : temp.entrySet()) {
                String movieID = (String) mapSubElement.getKey();
                List<String> cap_data = (List<String>) mapSubElement.getValue();
                // System.out.println(movieID);
                // System.out.println(cap_data);
                int count = 0;
                while (cap_data.contains(customerID)) {
                    if (!(result.contains("Outremont Movies : "))) {
                        result.add("Outremont Movies : ");
                    }
                    if (!(result.contains(movieName))) {
                        result.add(movieName);
                    }
                    // result.add(movieID);
                    cap_data.remove(customerID);
                    count++;
                }
                for (int i = 0; i < count; i++) {
                    cap_data.add(customerID);
                }
                if(cap_data.contains(customerID)){
                    result.add(movieID +"-"+Integer.toString(count));
                }
            }
        }
        // Another Server Result
        if ((customerID.substring(0, 3)).equals("OUT")) {
            String ver_res = requestAnotherServer("get_movieslots", params, ver_port);
            String atw_res = requestAnotherServer("get_movieslots", params, atw_port);
            List<String> ver_list = Stream.of(ver_res.split(",", -1)).collect(Collectors.toList());
            List<String> atw_list = Stream.of(atw_res.split(",", -1)).collect(Collectors.toList());
            if (ver_list.size() > 1) {
                result.addAll(ver_list);
            }
            if (atw_list.size() > 1) {
                result.addAll(atw_list);
            }
        }
        // Server Log
        try {
            serverLog("Customer " + customerID, "Get movie slot", customerID, "Success", String.join("-", result));
        } catch (IOException e) {
            System.out.println(e);
        }
        return result.toArray(new String[result.size()]);
    }


    public String cancelMovieTickets(String customerID, String movieID, int numberOfTickets) {
        // TODO Auto-generated method stub
        String result = "";
        String status = "";
        List<String> params = new ArrayList<String>();
        params.add(customerID);
        params.add(movieID);
        params.add(String.valueOf(numberOfTickets));
        if ((movieID.substring(0, 3)).equals("OUT")) {
            int flag = 0;
            for (Map.Entry mapElement : app_data.entrySet()) {
                String appType = (String) mapElement.getKey();
                Map<String, List<String>> temp = (Map<String, List<String>>) mapElement.getValue();
                // System.out.println(appType);
                // System.out.println(temp);
                for (Map.Entry mapSubElement : temp.entrySet()) { // check Movie ID is there or not
                    String appID = (String) mapSubElement.getKey();
                    if (appID.equals(movieID)) { // if Movie ID is there check the right customer is trying to
                                                 // cancel the app...
                        List<String> cap_data = (List<String>) mapSubElement.getValue();
                        if (cap_data.contains(customerID)) { // if yes then

                            int capacity = Integer.parseInt(cap_data.get(0));

                            capacity = capacity + numberOfTickets;
                            cap_data.set(0, Integer.toString(capacity));
                            while (numberOfTickets > 0) {
                                cap_data.remove(customerID); // remove the movie booking
                                numberOfTickets--;
                            }
                            result = "The Movie Ticket has been cancelled for the customer " + customerID;
                            status = "Success";
                        } else {
                            result = "The Movie booking is not booked by " + customerID
                                    + " so he/she cannot cancel it ";
                            status = "Failed";
                        }
                        flag = 1;
                        break;
                    } else {
                        result = "The Movie ticket " + movieID + " does not exist ";
                        status = "Failed";
                    }
                }
                if (flag == 1)
                    break;
            }
        } else if ((movieID.substring(0, 3)).equals("ATW")) {
            result = requestAnotherServer("cancel_movie", params, atw_port);
            if (result.equals("The Movie Ticket has been cancelled for the customer " + customerID))
                status = "Success";
            else
                status = "Failed";
        } else if ((movieID.substring(0, 3)).equals("VER")) {
            result = requestAnotherServer("cancel_movie", params, ver_port);
            if (result.equals("The Movie Ticket has been cancelled for the customer " + customerID))
                status = "Success";
            else
                status = "Failed";
        }
        // Server Log
        try {
            serverLog("Customer " + customerID, "Cancel an movie Ticket", customerID + " " + movieID, status, result);
        } catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String result = "";
        String status = "";
        List<String> params = new ArrayList<String>();
        params.add(customerID);
        params.add(movieID);
        params.add(movieName);
        params.add(String.valueOf(numberOfTickets));
        if ((movieID.substring(0, 3)).equals("OUT")) {
            int flag = 0;
            for (Map.Entry mapElement : app_data.entrySet()) {
                String mName = (String) mapElement.getKey();
                Map<String, List<String>> temp = (Map<String, List<String>>) mapElement.getValue();

                for (Map.Entry mapSubElement : temp.entrySet()) { // check movieID is there or not
                    String mID = (String) mapSubElement.getKey();
                    if (mID.equals(movieID)) { // if moviID is there check the right customer is trying to
                                               // cancel the movie...
                        List<String> cap_data = (List<String>) mapSubElement.getValue();
                        if (cap_data.contains(customerID)) { // if yes then
                            int count=0;
                            int capacity = Integer.parseInt(cap_data.get(0));

                            while (cap_data.contains(customerID)) {
                            
                                cap_data.remove(customerID); // remove the movie booking
                                count++;
                            }
                            if(count < numberOfTickets || count < 0){
                                for(int i=0;i<count;i++){
                                    cap_data.add(customerID);
                                }
                                result = "The Movie is not booked by " + customerID + " so he/she cannot exchange more then booked ";
                                status = "Failed";
                            }else{
                                capacity = capacity + count;
                                cap_data.set(0, Integer.toString(capacity));
                                result = "The Movie Ticket has been cancelled for the customer " + customerID;
                                status = "Success";
                            }
                        } else {
                            result = "the Movie is not booked by " + customerID + " so he/she cannot cancel it ";
                            status = "Failed";
                        }
                        flag = 1;
                        break;
                    } else {
                        result = "the Movie " + movieID + " does not exist ";
                        status = "Failed";
                    }
                }
                if (flag == 1)
                    break;
            }
        } else if ((movieID.substring(0, 3)).equals("VER")) {
            result = requestAnotherServer("cancel_movie", params, ver_port);
            if (result.equals("the Movie booking has been cancelled for the customer " + customerID))
                status = "Success";
            else
                status = "Failed";
        } else if ((movieID.substring(0, 3)).equals("ATW")) {
            result = requestAnotherServer("cancel_movie", params, atw_port);
            if (result.equals("the Movie booking has been cancelled for the customer " + customerID))
                status = "Success";
            else
                status = "Failed";
        }
        // Server Log
        try {
            serverLog("Customer " + customerID, "Cancel an movie booking", customerID + " " + movieID, status, result);
        } catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    @Override
    public String exchangeTickets(String customerID, String oldMovieID, String newMovieID, String newMovieName,
            int numberOfTickets) {
        // TODO Auto-generated method stub
        String result = "";
        String status = "";
        String cancelStatus = this.cancelMovieTickets(customerID, oldMovieID, newMovieName, numberOfTickets);
        String bookStatus = this.bookMovieTickets(customerID, newMovieID, newMovieName, numberOfTickets);
        boolean bookOperation = bookStatus.equals("Movie booked Successfully");
        boolean cancelOperation = cancelStatus
                .equals("The Movie Ticket has been cancelled for the customer " + customerID);
        if (bookOperation && cancelOperation) { // both operation are successful
            this.increaseBookingCapacity(oldMovieID, newMovieName);
            result = "The Movie Ticket has been exchanged for the customer " + customerID + " from old movie id "
                    + oldMovieID + " to " + newMovieID;
            status = "Success";
        } else if (bookOperation && !(cancelOperation)) { // booking has done but cancel operation is not done
            this.cancelMovieTickets(customerID, newMovieID, numberOfTickets);
            result = cancelStatus + " that's why exchange operation cannot be performed";
            status = "Failed";
        } else if (cancelOperation && !(bookOperation)) { // cancel operation has done but booking is unsuccessful
            this.increaseBookingCapacity(oldMovieID, newMovieName);
            this.bookMovieTickets(customerID, oldMovieID, newMovieName, numberOfTickets);
            result = bookStatus + " that's why exchange operation cannot be performed";
            status = "Failed";
        } else { // both operation failed
            result = "This operation cannot be perfomed by the customer " + customerID
                    + " because he/she neither booked the tickets nor canceled it";
            status = "Failed";
        }
        try {
            serverLog("Customer " + customerID, "Exchanged an Movie ticket",
                    customerID + " " + oldMovieID + " " + newMovieName + " " + newMovieID + " " + newMovieName, status,
                    result);
        } catch (IOException e) {
            System.out.println(e);
        }
        return result;
    }

    public void increaseBookingCapacity(String movieID, String movieName) {
        List<String> params = new ArrayList<String>();
        params.add(movieID);
        params.add(movieName);
        if ((movieID.substring(0, 3)).equals("OUT")) {
            if (app_data.containsKey(movieName)) {
                Map<String, List<String>> temp = app_data.get(movieName);
                if (temp.containsKey(movieID)) {
                    List<String> cap_data = temp.get(movieID);
                    int capacity = Integer.parseInt(cap_data.get(0));
                    // capacity++;
                    cap_data.set(0, Integer.toString(capacity));
                }
            }
        } else if ((movieID.substring(0, 3)).equals("ATW")) {
            requestAnotherServer("increase_tickets", params, atw_port);
        } else if ((movieID.substring(0, 3)).equals("VER")) {
            requestAnotherServer("increase_tickets", params, ver_port);
        }
    }

    @Override
    public void shutdown() {
        // TODO Auto-generated method stub
        orb.shutdown(false);
    }
}

public class OutremontServer {
    public static void main(String[] args) {

        try {
            ORB orb = ORB.init(args, null);
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            OUTImpl outImpl = new OUTImpl();
            outImpl.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(outImpl);
            DMTBS href = DMTBSHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            // bind the Object Reference in Naming
            String name = "outremont";
            NameComponent path[] = ncRef.to_name(name);
            ncRef.rebind(path, href);

            System.out.println("Outremont Server ready and waiting ...");

            Runnable UDPServer = new Runnable() {
                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    receiveRequestFromAnotherServer(outImpl);
                }
            };
            Thread t = new Thread(UDPServer);
            t.start();

            while (true) {
                orb.run();
            }

        } catch (Exception e) {
            System.err.println("ERROR: " + e);
            e.printStackTrace(System.out);
        }

    }

    public static void receiveRequestFromAnotherServer(OUTImpl outobj) {
        String result = "";
        DatagramSocket ds = null;
        try {
            ds = new DatagramSocket(6321);
            byte[] receive = new byte[5000];
            System.out.println("UDP Server for Outremont is running on port : 6321");
            while (true) {
                DatagramPacket DpReceive = null;
                DpReceive = new DatagramPacket(receive, receive.length);
                ds.receive(DpReceive);
                String received_func_str = new String(DpReceive.getData(), 0, DpReceive.getLength());
                // System.out.println(received_func_str);//<----------------------------------------------------------------------------------------------testing
                String[] temp = received_func_str.split("@");
                String func = temp[0];
                // System.out.println(func);
                // //<----------------------------------------------------------------------------------------------testing
                // String[] received_parameters = temp[1].split(",");
                List<String> parameters = new ArrayList<String>(Arrays.asList(temp[1].split(",")));
                // System.out.println(received_parameters);
                // //<----------------------------------------------------------------------------------------------testing
                // System.out.println(parameters);
                if (func.equals("list_movieslots")) {
                    List<String> ls = new ArrayList<String>();
                    ls = outobj.listMovieAvailabilitySupport(parameters.get(0));
                    result = String.join(",", ls).trim();
                } else if (func.equals("book_movie")) {
                    result = outobj.bookMovieTickets(parameters.get(0), parameters.get(1), parameters.get(2),
                            Integer.parseInt(parameters.get(3))); // issue
                } else if (func.equals("get_movieslots")) {
                    List<String> data = new ArrayList<String>();
                    String[] datastringarray = new String[256];
                    datastringarray = outobj.getBookingSchedule(parameters.get(0));
                    data = Arrays.asList(datastringarray);
                    result = String.join(",", data).trim();
                } else if (func.equals("cancel_movie")) {
                    if (parameters.size() == 3)
                        result = outobj.cancelMovieTickets(parameters.get(0), parameters.get(1),
                                Integer.parseInt(parameters.get(2)));
                    if (parameters.size() == 4)
                        result = outobj.cancelMovieTickets(parameters.get(0), parameters.get(1), parameters.get(2),
                                Integer.parseInt(parameters.get(3)));
                } else if (func.equals("increase_tickets")) {
                    outobj.increaseBookingCapacity(parameters.get(0), parameters.get(1));
                }

                result += "@";
                byte[] send_result = result.getBytes();
                DatagramPacket send_reply = new DatagramPacket(send_result, send_result.length, DpReceive.getAddress(),
                        DpReceive.getPort());
                ds.send(send_reply);
            }
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (ds != null)
                ds.close();
        }
    }
}
