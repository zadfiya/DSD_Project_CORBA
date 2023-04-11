/**

* @author  Krutik Gevariya

*/

package Replica1.ServerImplementation;
import Replica1.DMTBSapp.*;
import Replica1.Logger.Logger;
import Replica2.CommonOutput;
import org.omg.CORBA.ORB;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;



public class ServerImpl extends DMTBSPOA {


    private ORB orb;
    public static final int Atwater_Server_Port = 8888;
    public static final int Verdun_Server_Port = 7777;
    public static final int Outremont_Server_Port = 6666;

    // HashMap<movieName, HashMap <movieID, MovieData>>
    private Map<String, Map<String, Model.MovieData>> data;

    // HashMap<CustomerID, HashMap <movieName, Map<MovieID,numOfTickets>>>
    private Map<String, Map<String, Map<String,Integer>>> clientMovies;

    // HashMap<ClientID, Client>
    private Map<String, Model.ClientData> serverClients;



    private String serverID;
    private String serverName;

    public ServerImpl(String serverID, String serverName){
        this.serverID = serverID;
        this.serverName = serverName;
        data = new ConcurrentHashMap<>();
        data.put("Avatar",new ConcurrentHashMap<>());
        data.put("Avenger",new ConcurrentHashMap<>());
        data.put("Titanic",new ConcurrentHashMap<>());
        clientMovies = new ConcurrentHashMap<>();
        serverClients = new ConcurrentHashMap<>();
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }
    private static int getServerPort(String branchAcronym) {
        if (branchAcronym.equalsIgnoreCase("ATW")) {
            return Atwater_Server_Port;
        } else if (branchAcronym.equalsIgnoreCase("VER")) {
            return Verdun_Server_Port;
        } else if (branchAcronym.equalsIgnoreCase("OUT")) {
            return Outremont_Server_Port;
        }
        return 1;
    }
    @Override
    public String addMovieSlots(String movieID, String movieName, int bookingCapacity){
        String response;
        if(data.get(movieName).containsKey(movieID))
        {
            if(data.get(movieName).get(movieID).getMovieCapacity() < bookingCapacity) {
                data.get(movieName).get(movieID).setMovieCapacity(bookingCapacity);
                response = "Success: Movie " + movieID + " Capacity increased to " + bookingCapacity;
                try{
                    Logger.serverLog(serverID, "null", " Corba addMovie ", " movieID: " + movieID + " movieType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                }
                catch (Exception e)
                {
                    System.out.println(e);
                }
                return CommonOutput.addMovieSlotOutput(true, CommonOutput.addMovieSlot_success_added);
            }else{
                response = "Failed: Movie Already Exists, Cannot Decrease Booking Capacity";
                try {
                    Logger.serverLog(serverID, "null", " RMI addMovie ", " movieID: " + movieID + " movieType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.addMovieSlotOutput(false, null);
            }
        }
        if(Model.MovieData.detectMovieServer(movieID).equals(serverName)){
            Model.MovieData newData = new Model.MovieData(movieID,movieName,bookingCapacity);
            Map<String, Model.MovieData> movieHashMap = data.get(movieName);
            movieHashMap.put(movieID,newData);
            data.put(movieID,movieHashMap);
            response = "Success: Movie " + movieID + " added successfully";
            try {
                Logger.serverLog(serverID, "null", " RMI addMovie ", " movieID: " + movieID + " movieType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addMovieSlotOutput(true, CommonOutput.addMovieSlot_success_added);
        }else{
            response = "Failed: Cannot Add Movie to servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " RMI addMovie ", " movieID: " + movieID + " movieType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addMovieSlotOutput(false, null);
        }
    }

    public String removeMovieSlots(String movieID,String movieName){
        String response;
        if(Model.MovieData.detectMovieServer(movieID).equals(serverName)){
            if(data.get(movieName).containsKey(movieID)){
                Map<String,Integer> registeredCustomer = data.get(movieName).get(movieID).getRegisteredCustomer();
                System.out.println(1 + " " +registeredCustomer.toString());
                data.get(movieName).remove(movieID);
                System.out.println(1 + " " +registeredCustomer.toString());
                addCustomerToNextSameMovie(movieID,movieName,registeredCustomer);
                System.out.println(1 + " " +registeredCustomer.toString());
                response = "Success: Movie Removed Successfully";
                try {
                    Logger.serverLog(serverID, "null", " RMI removeMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                System.out.println(clientMovies + " in remove movie" + serverName);
                return CommonOutput.removeMovieSlotOutput(true, null);
            }else{
                response = "Failed: Movie " + movieID + " Does Not Exist";
                try {
                    Logger.serverLog(serverID, "null", " RMI removeMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.removeMovieSlotOutput(false, CommonOutput.removeMovieSlot_fail_no_such_movieShow);
            }
        }else{
            response = "Failed: Cannot Remove Movie from servers other than " + serverName;
            try {
                Logger.serverLog(serverID, "null", " RMI removeMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.removeMovieSlotOutput(false, CommonOutput.removeMovieSlot_fail_no_such_movieShow);
        }
    }

    private void addCustomerToNextSameMovie(String oldmovieID, String movieName, Map<String,Integer> registeredCustomer) {
        for(String customerID : registeredCustomer.keySet()){
            if(customerID.substring(0,3).equals(serverID)){
                clientMovies.get(customerID).get(movieName).remove(oldmovieID);
                String nextSameEventResult = getNextSameEvent(data.get(movieName).keySet(),movieName,oldmovieID, registeredCustomer.get(customerID));
                if(nextSameEventResult.equals("Failed")){
                    return;
                }else{
                    bookMovieTickets(customerID,nextSameEventResult,movieName,registeredCustomer.get(customerID));
                }
            }
            else{
                if(sendUDPMessage(getServerPort(customerID.substring(0, 3)), "removeMovie", customerID, movieName, oldmovieID,registeredCustomer.get(customerID)).charAt(0) == 'S'){
                    clientMovies.get(customerID).get(movieName).remove(oldmovieID);
                    String nextSameEventResult = getNextSameEvent(data.get(movieName).keySet(),movieName,oldmovieID, registeredCustomer.get(customerID));
                    sendUDPMessage(getServerPort(customerID.substring(0, 3)), "bookMovie", customerID, movieName, nextSameEventResult,registeredCustomer.get(customerID));

                }

            }
        }
//        System.out.println(clientMovies + " in get next " + serverName);
    }

    private String getNextSameEvent(Set<String> keySet, String movieName, String oldMovieID,int numOfTickets){
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldMovieID);
        Collections.sort(sortedIDs, new Comparator<String>(){
            public int compare(String id1, String id2){
                Integer timeSlot1 = 0;
                switch(id1.substring(3,4).toUpperCase()){
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (id2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(id1.substring(8, 10) + id1.substring(6, 8) + id1.substring(4, 6));
                Integer date2 = Integer.parseInt(id2.substring(8, 10) + id2.substring(6, 8) + id2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeslotCompare = timeSlot1.compareTo(timeSlot2);
                if(dateCompare == 0){
                    return ((timeslotCompare == 0) ? dateCompare : timeslotCompare);
                }else{
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(oldMovieID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!data.get(movieName).get(sortedIDs.get(i)).isFull(numOfTickets)) {
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets){
        String response;

        if (!serverClients.containsKey(customerID)) {
            addNewCustomerToClients(customerID);
        }

        if(Model.MovieData.detectMovieServer(movieID).equals(serverName)){
            if(!data.get(movieName).containsKey(movieID)){
                response = "Failed: Movie " + movieID + " doest not exist";
                try {
                    Logger.serverLog(serverID, customerID, " RMI bookMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_no_capacity);

            }
            Model.MovieData movieData = data.get(movieName).get(movieID);
            if(!movieData.isFull(numberOfTickets)){
                if(clientMovies.containsKey(customerID)){
                    if (clientMovies.get(customerID).containsKey(movieName)) {
                        if (!clientMovies.get(customerID).get(movieName).keySet().contains(movieID)) {
                            clientMovies.get(customerID).get(movieName).put(movieID,numberOfTickets);
                            return CommonOutput.bookMovieTicketsOutput(true, null);

                        } else {
                            response = "Failed: Movie " + movieID + " Already Booked";
                            try {
                                Logger.serverLog(serverID, customerID, " RMI bookMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.bookMovieTicketsOutput(false, null);
                        }
                    }else{
                        Map<String,Integer> temp = new ConcurrentHashMap<>();
                        temp.put(movieID,numberOfTickets);
                        clientMovies.get(customerID).put(movieName,temp);
                        return CommonOutput.bookMovieTicketsOutput(true, null);

                    }
                }else{

                    Map<String, Integer> temp1 = new ConcurrentHashMap<>();
                    Map<String, Map<String,Integer>> temp2 = new ConcurrentHashMap<>();
                    temp1.put(movieID,numberOfTickets);
                    temp2.put(movieName,temp1);
                    clientMovies.put(customerID,temp2);
                }if(data.get(movieName).get(movieID).setRegisteredCustomer(customerID,numberOfTickets) == 1){

                    response = "Success: Movie " + movieID + " Booked Successfully";
                    return CommonOutput.bookMovieTicketsOutput(true, null);

                }else if(data.get(movieName).get(movieID).setRegisteredCustomer(customerID,numberOfTickets) == 3){
                    response = "Failed: " + movieID + " You Can not do that.";
                    return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_no_capacity);

                }else{
                    response = "Failed: Cannot Add You To Movie " + movieID;
                }
//                System.out.println(clientMovies + " in book movie" + serverName +  " " + 2);
                try {
                    Logger.serverLog(serverID, customerID, " RMI bookMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                response = "Failed: Movie " + movieID + " Limit exceed";
                try {
                    Logger.serverLog(serverID, customerID, " RMI bookMovie", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookMovieTicketsOutput(false, null);

            }
        }else{
            if(!exceedWeeklyLimit(customerID, movieID.substring(4))){

                String serverResponse = sendUDPMessage(getServerPort(movieID.substring(0, 3)), "bookMovie", customerID, movieName, movieID,numberOfTickets);
                if(serverResponse.startsWith("Success:")){
                    if(clientMovies.get(customerID).keySet().contains(movieName)){
                        System.out.println(serverName + " " + clientMovies);
                        clientMovies.get(customerID).get(movieName).put(movieID,numberOfTickets);
                        return CommonOutput.bookMovieTicketsOutput(true, null);

                    }else{
                        Map<String,Integer> temp = new ConcurrentHashMap<>();
                        temp.put(movieID,numberOfTickets);
                        clientMovies.get(customerID).put(movieName,temp);
                        System.out.println(serverName + " " + clientMovies + " " + 2 ) ;
                        return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_weekly_limit);

                    }
                }
//                System.out.println(clientMovies + " in book Movie" + serverName  + " " + 1);
                try {
                    Logger.serverLog(serverID, customerID, " RMI bookMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookMovieTicketsOutput(true, null);

            } else{
                response = "Failed: You Cannot Book Movie in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Logger.serverLog(serverID, customerID, " RMI bookMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_weekly_limit);
            }
        }

        return CommonOutput.bookMovieTicketsOutput(true, null);

    }

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numOfTickets){
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID + ";" + Integer.toString(numOfTickets);
        try {
            Logger.serverLog(serverID, customerID, " UDP request sent " + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message,dataFromClient.length(),aHost,serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer,buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];


        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        }finally {
            if(aSocket != null){
                aSocket.close();
            }
        }
        try {
            Logger.serverLog(serverID, customerID, " UDP reply received" + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(result);
        return result;
    }

    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numOfTickets , String OldMovieID){
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID + ";" + Integer.toString(numOfTickets) + ";" +OldMovieID;
        try {
            Logger.serverLog(serverID, customerID, " UDP request sent " + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message,dataFromClient.length(),aHost,serverPort);
            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer,buffer.length);

            aSocket.receive(reply);
            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];


        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        }finally {
            if(aSocket != null){
                aSocket.close();
            }
        }
        try {
            Logger.serverLog(serverID, customerID, " UDP reply received" + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // System.out.println(result);
        return result;
    }

    private boolean exceedWeeklyLimit(String customerID, String eventDate) {
        int limit = 0;
        for(int i=0;i<3;i++){
            List<String> registeredIDs = null;
            switch(i){
                case 0:
                    if(clientMovies.get(customerID).containsKey("Avatar")){
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Avatar").keySet());
                        System.out.println("Hello");
                    }
                    break;
                case 1:
                    if(clientMovies.get(customerID).containsKey("Titanic")){
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Titanic").keySet());
                    }
                    break;
                case 2:
                    if(clientMovies.get(customerID).containsKey("Avenger")){
                        registeredIDs = new ArrayList<>(clientMovies.get(customerID).get("Avenger").keySet());
                    }
                    break;
            }
            System.out.println(registeredIDs);
            if(registeredIDs != null){
                for (String eventID : registeredIDs) {
                    if (eventID.substring(6, 8).equals(eventDate.substring(2, 4)) && eventID.substring(8, 10).equals(eventDate.substring(4, 6))) {
                        int week1 = Integer.parseInt(eventID.substring(4, 6)) / 7;
                        int week2 = Integer.parseInt(eventDate.substring(0, 2)) / 7;
                        if (week1 == week2) {
                            limit++;
                        }
                    }
                    if (limit == 3)
                        return true;
                }
            }
        }
        return false;
    }

    public void addNewCustomerToClients(String customerID) {
        Model.ClientData newCustomer = new Model.ClientData(customerID);
        serverClients.put(newCustomer.getClientID(), newCustomer);
        clientMovies.put(newCustomer.getClientID(), new ConcurrentHashMap<>());
    }

    public String listMovieShowsAvailability(String movieName){
        String response;
        List<String> allMovieShowIDsWithCapacity = new ArrayList<>();
        Map<String, Model.MovieData> movieDataMap = data.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + movieName + ": \n");
        if(movieDataMap.size() == 0){
            builder.append("No Movies of the name " + movieName);
        }else{
            movieDataMap.entrySet().forEach(items -> {
                allMovieShowIDsWithCapacity.add(items.getValue().getMovieID() + " " + items.getValue().getMovieRemainingCapacity());
                builder.append(items.getValue().toString() + " ||\n");
                //builder.append(items.getKey() + " "+ items.getValue().getBookingCapacity() + " & Booked seats: "+ items.getValue().getBookedSeats()+" || ");
            });
            builder.append("\n=====================================\n");
        }
        String otherServer1, otherServer2;
        if(serverID.equals("ATW")){
            otherServer1 = sendUDPMessage(Verdun_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
            otherServer2 = sendUDPMessage(Outremont_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
        }else if (serverID.equals("OUT")) {
            otherServer1 = sendUDPMessage(Verdun_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
            otherServer2 = sendUDPMessage(Atwater_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
        }else{
            otherServer1 = sendUDPMessage(Atwater_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
            otherServer2 = sendUDPMessage(Outremont_Server_Port, "listMovieAvailability", "null", movieName, "null",0);
        }
        builder.append(otherServer1).append(otherServer2);
        response =builder.toString();
        try {
            Logger.serverLog(serverID, "null", " RMI listMovieAvailability ", " movieType: " + movieName + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonOutput.listMovieShowAvailabilityOutput(true, allMovieShowIDsWithCapacity, null);

    }

    public String listMovieShowsAvailabilityUDP(String movieName) {
        Map<String, Model.MovieData> dataMap = data.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + movieName + ":\n");
        if (dataMap.size() == 0) {
            builder.append("No Movies of the name " + movieName);
        } else {
            for (Model.MovieData md :
                    dataMap.values()) {
                builder.append(md.toString() + " || ");
            }
        }
        builder.append("\n=====================================\n");
        return builder.toString();
    }

    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String response;

        if(Model.MovieData.detectMovieServer(movieID).equalsIgnoreCase(serverName)){
            if(customerID.substring(0,3).equals(serverID)){
                if(!serverClients.containsKey(customerID)){
                    addNewCustomerToClients(customerID);
                    response ="Failed: Customer " + customerID + "Are Not Registered in " + movieID;
                    try {
                        Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

                }else{
                    if(!clientMovies.get(customerID).get(movieName).containsKey(movieID)){
                        response ="Failed: Customer " + customerID + "Are Not Registered in " + movieID;
                        try {
                            Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

                    }else{
                        int tickets =  clientMovies.get(customerID).get(movieName).get(movieID);
                        if(numberOfTickets == tickets){
                            clientMovies.get(customerID).get(movieName).remove(movieID);
                            data.get(movieName).get(movieID).removeRegisteredCustomer(customerID);
                            response = "Success: Movie " + movieID + " Canceled for " + customerID;
                            try {
                                Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.cancelMovieTicketsOutput(false, null);
                        }else if(numberOfTickets > tickets){
                            response = "Failed : Customer have only booked " + tickets + "so you cannot cancel " + numberOfTickets;
                            try {
                                Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

                        }else{
                            int updatedTickets = tickets - numberOfTickets;
                            clientMovies.get(customerID).get(movieName).put(movieID,updatedTickets);
                            data.get(movieName).get(movieID).setRegisteredCustomer(customerID,updatedTickets);
                            response = "Success: Movie " + movieID + " Canceled for " + customerID;
                            try {
                                Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.cancelMovieTicketsOutput(true, null);

                        }
                    }
                }
            }else{
                int tickets =  clientMovies.get(customerID).get(movieName).get(movieID);
                if(numberOfTickets == tickets){
                    clientMovies.get(customerID).get(movieName).remove(movieID);
                    data.get(movieName).get(movieID).removeRegisteredCustomer(customerID);
                    response = "Success: Movie " + movieID + " Canceled for " + customerID;
                    try {
                        Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(true, null);

                }else if(numberOfTickets > tickets){
                    response = "Failed : Customer have only booked " + tickets + "so you cannot cancel " + numberOfTickets;
                    try {
                        Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

                }else{
                    int updatedTickets = tickets - numberOfTickets;
                    clientMovies.get(customerID).get(movieName).put(movieID,updatedTickets);
                    data.get(movieName).get(movieID).setRegisteredCustomer(customerID,updatedTickets);
                    response = "Success: Movie " + movieID + " Canceled for " + customerID;
                    try {
                        Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(true, null);

                }
            }
        }else{
            if(customerID.substring(0,3).equals(serverID)){
                if(!serverClients.containsKey(customerID)){
                    addNewCustomerToClients(customerID);
                }else{
                    int tickets =  clientMovies.get(customerID).get(movieName).get(movieID);
                    if(numberOfTickets == tickets){
                        clientMovies.get(customerID).get(movieName).remove(movieID);
                        response = sendUDPMessage(getServerPort(movieID.substring(0,3)),"cancelMovie",customerID,movieName,movieID,numberOfTickets) ;
                    }else if(numberOfTickets > tickets){
                        response = sendUDPMessage(getServerPort(movieID.substring(0,3)),"cancelMovie",customerID,movieName,movieID,numberOfTickets) ;;
                        return response;
                    }else{
                        int updatedTickets = tickets - numberOfTickets;
                        clientMovies.get(customerID).get(movieName).put(movieID,updatedTickets);
                        response = sendUDPMessage(getServerPort(movieID.substring(0,3)),"cancelMovie",customerID,movieName,movieID,numberOfTickets) ;;
                    }
                    return response;
                }
            }
            try {
                Logger.serverLog(serverID, customerID, " RMI cancelMovie ", " movieID: " + movieID + " movieType: " + movieName + " ", "Failed: You " + customerID + " Are Not Registered in " + movieID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Failed: You " + customerID + " Are Not Registered in " + movieID;
        }
    }

    @Override
    public String exchangeTickets(String customerID, String oldMovieID, String new_movieID, String new_movieName, int numberOfTickets) {
        String response;
//        if (!checkClientExists(customerID)) {
//            response = "Failed: You " + customerID + " Are Not Registered in " + oldEventID;
//            try {
//                Logger.serverLog(serverID, customerID, " CORBA swapEvent ", " oldEventID: " + oldEventID + " oldEventType: " + oldEventType + " newEventID: " + newEventID + " newEventType: " + newEventType + " ", response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return response;
//        } else {
        if(detectServer(oldMovieID).equals(serverName))
        {
            if (clientHasMovieShow(customerID, new_movieName, oldMovieID)) {
                String bookResp = "Failed: did not send book request for your new Movie " + new_movieID;
                String cancelResp = "Failed: did not send cancel request for your old Movie " + oldMovieID;
                synchronized (this) {
                    if (onTheSameWeek(new_movieID.substring(4), oldMovieID) && !exceedWeeklyLimit(customerID, new_movieID.substring(4))) {
                        cancelResp = cancelMovieTickets(customerID, oldMovieID, new_movieName, numberOfTickets);
                        System.out.println("cancel Response");
                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookMovieTickets(customerID, new_movieID, new_movieName,numberOfTickets);
                        }
                    } else {
                        bookResp = bookMovieTickets(customerID, new_movieID, new_movieName,numberOfTickets);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelMovieTickets(customerID, oldMovieID, new_movieName,numberOfTickets);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Event " + oldMovieID + " swapped with " + new_movieID;
                    return CommonOutput.exchangeTicketsOutput(true,null);

                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    bookMovieTickets(customerID, new_movieID, new_movieName,numberOfTickets);
                    response = "Failed: Your oldEvent " + oldMovieID + " Could not be Canceled reason: " + cancelResp;
                    return CommonOutput.exchangeTicketsOutput(false,null);

                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    //hope this won't happen, but just in case.
                    String resp1 = bookMovieTickets(customerID, oldMovieID, new_movieName,numberOfTickets);
                    response = "Failed: Your newEvent " + new_movieID + " Could not be Booked reason: " + bookResp + " And your oldMovieID Rolling back: " + resp1;
                    return CommonOutput.exchangeTicketsOutput(false,null);

                } else {
                    response = "Failed: on Both newEvent " + new_movieID + " Booking reason: " + bookResp + " and oldMovieID " + oldMovieID + " Canceling reason: " + cancelResp;

                    try {
                        Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + new_movieName + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.exchangeTicketsOutput(false,null);

                }

            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
                try {
                    Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + new_movieName + " new_movieID: " + new_movieID + " new_movieName: " + new_movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.exchangeTicketsOutput(false,null);

            }
        }
        else
        {
            response = sendUDPMessage(getServerPort(oldMovieID.substring(0, 3)), "exchangeTickets", customerID, new_movieName, new_movieID,numberOfTickets,oldMovieID);
            try {
                Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " MovieId: " + new_movieID + " movieName: " + new_movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.exchangeTicketsOutput(true,null);

        }

        //}

    }

    public static String detectServer(String movieID)
    {  if (movieID.substring(0, 3).equalsIgnoreCase("ATW")) {
        return "Atwater";
    } else if (movieID.substring(0, 3).equalsIgnoreCase("VER")) {
        //return MovieManagement.THEATER_SERVER_VERDUN;
        return "Verdun";
    } else {
        return "Outremont";
        //return MovieManagement.THEATER_SERVER_OUTREMONT;
    } }
    public static boolean onTheSameWeek(String newMovieDate, String movieID) {
        if (movieID.substring(6, 8).equals(newMovieDate.substring(2, 4)) && movieID.substring(8, 10).equals(newMovieDate.substring(4, 6))) {
            int week1 = Integer.parseInt(movieID.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newMovieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(day2 - day1);
            return week1 == week2;
        } else {
            return false;
        }
    }
    @Override
    public void shutdown() {
        orb.shutdown(false);
    }

//    private synchronized boolean checkClientExists(String customerID) {
//        if (!serverClients.containsKey(customerID)) {
//            addNewCustomerToClients(customerID);
//            return false;
//        } else {
//            return true;
//        }
//    }

    private synchronized boolean clientHasMovieShow(String customerID, String movieName, String movieID) {
        if (clientMovies.containsKey(customerID) && clientMovies.get(customerID).containsKey(movieName)) {
            return clientMovies.get(customerID).get(movieName).containsKey(movieID);
        } else {
            return false;
        }
    }


    public String getBookingSchedule(String customerID){
        System.out.println(clientMovies + " " + serverName);
        String response;
        Map<String,Map<String,Integer>> movies = clientMovies.get(customerID);
        if(!serverClients.containsKey(customerID)){
            addNewCustomerToClients(customerID);
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.getBookingScheduleOutput(false, movies , null);

        }

        if(movies.size() == 0){
            response = "Booking Schedule Empty For " + customerID;
            try {
                Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.getBookingScheduleOutput(false, movies , null);

        }
        StringBuilder builder = new StringBuilder();
        System.out.println(clientMovies);
        for(String movieName : movies.keySet()){
            builder.append(movieName + ":\n");
            for(String movieID : movies.get(movieName).keySet()){
                builder.append(movieID + " || ");
            }
            builder.append("\n=====================================\n");
        }
        response = builder.toString();
        try {
            Logger.serverLog(serverID, customerID, " RMI getBookingSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonOutput.getBookingScheduleOutput(true, movies, null);

    }

    public String removeMovieSlotsUDP(String oldMovieID,String movieName,String customerID, int numberOfTickets){
        if(!serverClients.containsKey(customerID)){
            addNewCustomerToClients(customerID);
            return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
        }else{
            if(clientMovies.get(customerID).get(movieName).remove(oldMovieID,numberOfTickets)){
//                Map<String,Integer> map = new ConcurrentHashMap<>();
//                map.put(customerID,numberOfTickets);
//                addCustomerToNextSameMovie(oldMovieID,movieName,map);
                System.out.println(data.get(movieName).keySet());
                return "Success: Movie " + oldMovieID + " Was Removed from " + customerID + " Schedule";
            }else{
                return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
            }
        }
    }



}
