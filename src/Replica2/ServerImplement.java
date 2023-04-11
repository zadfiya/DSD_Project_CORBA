package Replica2;

import Replica2.ServerObjectInterfaceApp2.ServerObjectInterface2POA;
import org.omg.CORBA.ORB;
/**
 * Team 22
 */
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ServerImplement extends ServerObjectInterface2POA {

    private ORB orb;
    public static final int ATWATER_SERVER_PORT = 8888;
    public static final int VERDUN_SERVER_PORT = 7777;
    public static final int OUTREMONT_SERVER_PORT = 6666;
    public static final String THEATER_SERVER_ATWATER = "ATWATER";
    public static final String THEATER_SERVER_VERDUN = "VERDUN";
    public static final String THEATER_SERVER_OUTREMONT = "OUTREMONT";
    private String serverID;
    private String serverName;
    private Map<String, Map<String, MovieModel>> allMovieShows;

    private Map<String, Map<String, Map<String, Integer>>> clientEvents;


    /**
     *
     * @param serverID
     * @param serverName
     */
    public ServerImplement(String serverID, String serverName)  {
        super();
        this.serverID = serverID;
        this.serverName = serverName;
        allMovieShows = new ConcurrentHashMap<>();
        allMovieShows.put(Constant.MOVIE_AVENGER, new ConcurrentHashMap<>());
        allMovieShows.put(Constant.MOVIE_AVTAR, new ConcurrentHashMap<>());
        allMovieShows.put(Constant.MOVIE_TITANIC, new ConcurrentHashMap<>());
        clientEvents = new ConcurrentHashMap<>();
        //serverClients = new ConcurrentHashMap<>();
       addTestData();
    }

    private void addTestData() {
        clientEvents.put(serverID+"C1111", new ConcurrentHashMap<>());
        //System.out.println(this.serverID +" Srever ID");
        MovieModel avtar = new MovieModel(Constant.MOVIE_AVTAR, serverID + "A100223", 20);
        avtar.addRegisteredClientID(serverID+"C1111");
        clientEvents.get(serverID+"C1111").put(Constant.MOVIE_AVTAR, new ConcurrentHashMap<>());
        clientEvents.get(serverID+"C1111").get(Constant.MOVIE_AVTAR).put(avtar.getMovieID(),3);

        MovieModel avenger = new MovieModel(Constant.MOVIE_AVENGER, serverID + "M100223", 20);
        avenger.addRegisteredClientID(serverID+"C1111");
        clientEvents.get(serverID+"C1111").put(Constant.MOVIE_AVENGER, new ConcurrentHashMap<>());
        clientEvents.get(serverID+"C1111").get(Constant.MOVIE_AVENGER).put(avenger.getMovieID(),3);

        MovieModel titanic = new MovieModel(Constant.MOVIE_TITANIC, serverID + "E100223", 20);
        titanic.addRegisteredClientID(serverID+"C1111");
        clientEvents.get(serverID+"C1111").put(Constant.MOVIE_TITANIC, new ConcurrentHashMap<>());
        clientEvents.get(serverID+"C1111").get(Constant.MOVIE_TITANIC).put(titanic.getMovieID(),3);



        allMovieShows.get(Constant.MOVIE_AVTAR).put(avtar.getMovieID(), avtar);
        allMovieShows.get(Constant.MOVIE_AVENGER).put(avenger.getMovieID(), avenger);
        allMovieShows.get(Constant.MOVIE_TITANIC).put(titanic.getMovieID(), titanic);
    }

    /**
     *
     * @param orb_val
     */
    public void setORB(ORB orb_val) {
        orb = orb_val;
    }

    /**
     *
     * @param movieID
     * @param movieName
     * @param bookingCapacity
     * @return
     */
    @Override
    public String addMovieSlot(String movieID, String movieName, int bookingCapacity)  {
        String response;
        if (allMovieShows.get(movieName).containsKey(movieID)) {
            MovieModel movieSlot = allMovieShows.get(movieName).get(movieID);
            movieSlot.setBookingCapacity(bookingCapacity);
            allMovieShows.get(movieName).put(movieID, movieSlot);
            response = "Success: Movie Show " + movieID + " updated with number of booking seats " + bookingCapacity;
            System.out.println(serverName + ">>>" + response);
            try {
                Logger.serverLog(serverID, "null", " CORBA addMovieSlot ", " movieID: " + movieID + " eventType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addMovieSlotOutput(true, CommonOutput.addMovieSlot_success_capacity_updated);
        }

        if (Constant.detectServer(movieID).equals(serverName)) {
            MovieModel event = new MovieModel(movieName, movieID, bookingCapacity);
            Map<String, MovieModel> movieSlot = allMovieShows.get(movieName);
            movieSlot.put(movieID, event);
            //Map<String, Integer> movieHashMap = allMovieShows.get(movieName);
            //movieHashMap.put(movieID,bookingCapacity );
            allMovieShows.put(movieName, movieSlot);
            response = "Success: Movie " + movieID + " added successfully";
            System.out.println(serverName + ">>>" + response);
            try {
                Logger.serverLog(serverID, "null", " CORBA addMovieSlot ", " movieID: " + movieID + " eventType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addMovieSlotOutput(true, CommonOutput.addMovieSlot_success_added);
        } else {
            response = "Failed: Cannot Add MovieSlot to servers other than " + serverName;
            System.out.println(serverName + ">>>" + response);
            try {
                Logger.serverLog(serverID, "null", " CORBA addMovieSlot ", " movieID: " + movieID + " eventType: " + movieName + " bookingCapacity " + bookingCapacity + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.addMovieSlotOutput(false, CommonOutput.addMovieSlot_fail_another_server);
        }

    }

    /**
     *
     * @param movieID
     * @param movieName
     * @return
     */
    @Override
    public String removeMovieSlots(String movieID, String movieName) {
        String response;

        if (Constant.detectServer(movieID).equals(serverName)) {
            if (allMovieShows.get(movieName).containsKey(movieID)) {
                List<String> registeredClients = allMovieShows.get(movieName).get(movieID).getRegisteredClientIDs();
                allMovieShows.get(movieName).remove(movieID);


                addCustomersToNextSameEvent(movieID, movieName, registeredClients);


                response = "Success: Movie Show Removed Successfully";
                System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.removeMovieSlotOutput(true, CommonOutput.removeMovieSlot_success_removed);
            } else {
                response = "Failed: Movie " + movieID + " Does Not Exist";
                System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.removeMovieSlotOutput(false, CommonOutput.removeMovieSlot_fail_no_such_movieShow);
            }
        } else {
            response = "Failed: Cannot Remove Movie Show from servers other than " + serverName;
            System.out.println(serverName + ">>>" + response);
            try {
                Logger.serverLog(serverID, "null", " CORBA removeMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.removeMovieSlotOutput(false, CommonOutput.removeMovieSlot_fail_another_server);
        }

    }

    /**
     *
     * @param movieID
     * @param movieName
     * @return
     */
    public String removeMovieSlotsUDP(String movieID, String movieName){
        String response;


        if (allMovieShows.get(movieName).containsKey(movieID)) {
                List<String> registeredClients = allMovieShows.get(movieName).get(movieID).getRegisteredClientIDs();



                addCustomersToNextSameEvent(movieID, movieName, registeredClients);

                response = "Success: Movie Show Removed Successfully";

            System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return CommonOutput.removeMovieSlotOutput(true, CommonOutput.removeMovieSlot_success_removed);

        } else {
                response = "Failed: Movie " + movieID + " Does Not Exist";
            System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, "null", " CORBA removeMovieSlot ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            return CommonOutput.removeMovieSlotOutput(false, CommonOutput.removeMovieSlot_fail_no_such_movieShow);

        }


    }

    /**
     *
     * @param oldMovieID
     * @param movieName
     * @param registeredClients
     */
    private void addCustomersToNextSameEvent(String oldMovieID, String movieName, List<String> registeredClients) {

        for (String customerID :
                registeredClients) {
            int count = clientEvents.get(customerID).get(movieName).get(oldMovieID);

            if (customerID.substring(0, 3).equals(serverID)) {
                count = clientEvents.get(customerID).get(movieName).remove(oldMovieID);
                String nextSameEventResult = getNextSameEvent(allMovieShows.get(movieName).keySet(), movieName, oldMovieID);
                if (nextSameEventResult.equals("Failed")) {
                    System.out.println("Couldn't find another slot for same movie");
                    return;
                } else {
                    System.out.println("trying to book Movie for customer "+ customerID);
                    System.out.println(nextSameEventResult+" nextEventResult");

                        bookMovieTickets(customerID, nextSameEventResult, movieName, count);

                }
            } else {

                String nextSameEventResult = getNextSameEvent(allMovieShows.get(movieName).keySet(), movieName, oldMovieID);

                String result = sendUDPMessage(Constant.getUDPServerPort(customerID.substring(0, 3)), "removeMovieSlot", customerID, movieName, nextSameEventResult, count);
                count = clientEvents.get(customerID).get(movieName).remove(oldMovieID);

            }
        }
    }

    /**
     *
     * @param oldMovieID
     * @param movieName
     * @param registeredClients
     */
    private void addCustomersToNextSameEventUDP(String oldMovieID, String movieName, List<String> registeredClients)  {

        for (String customerID :
                registeredClients) {
            int count = clientEvents.get(customerID).get(movieName).get(oldMovieID);

            if (customerID.substring(0, 3).equals(serverID)) {
                count = clientEvents.get(customerID).get(movieName).remove(oldMovieID);
                String nextSameEventResult = getNextSameEvent(allMovieShows.get(movieName).keySet(), movieName, oldMovieID);
                if (nextSameEventResult.equals("Failed")) {
                    System.out.println("Couldn't find another slot for same movie");
                    return;
                } else {
                    System.out.println("trying to book Movie for customer "+ customerID);
                    bookMovieTickets(customerID, nextSameEventResult, movieName, count);
                }
            } else {
                System.out.println("Send UDP called for remove slot and add new Slot");
                String result = sendUDPMessage(Constant.getUDPServerPort(customerID.substring(0, 3)), "removeMovieSlot", customerID, movieName, oldMovieID, count);

            }
        }
    }

    /**
     *
     * @param keySet
     * @param movieName
     * @param oldMovieID
     * @return
     */
    private String getNextSameEvent(Set<String> keySet, String movieName, String oldMovieID) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(oldMovieID);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
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
                switch (ID2.substring(3, 4).toUpperCase()) {
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
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(oldMovieID) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allMovieShows.get(movieName).get(sortedIDs.get(i)).isFull()) {
                System.out.println(sortedIDs.get(i));
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }


    /**
     *
     * @param movieName
     * @return
     */
    @Override
    public String listMovieShowAvailability(String movieName){
        List<String> allMovieShowIDsWithCapacity = new ArrayList<>();
        Map<String, MovieModel> movieShows = allMovieShows.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server " + movieName + ":\n");

        if (movieShows.size() == 0) {
            builder.append("No Movie show for movie " + movieName);
        } else {
            movieShows.entrySet().forEach(items -> {
                //allMovieShowIDsWithCapacity.add(items.getValue().getMovieID() + " " + items.getValue().getTheaterRemainCapacity());

                builder.append(items.getValue().toString() + " ||\n");
                //builder.append(items.getKey() + " "+ items.getValue().getBookingCapacity() + " & Booked seats: "+ items.getValue().getBookedSeats()+" || ");
            });
            allMovieShowIDsWithCapacity.add(builder.toString()+" \n");
        } builder.append("\n=====================================\n");
        String otherServer1, otherServer2;
        if (serverID.equals("ATW")) {
            otherServer1 = sendUDPMessage(VERDUN_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(OUTREMONT_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
        } else if (serverID.equals("VER")) {
            otherServer1 = sendUDPMessage(ATWATER_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(OUTREMONT_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
        } else {
            otherServer1 = sendUDPMessage(ATWATER_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
            otherServer2 = sendUDPMessage(VERDUN_SERVER_PORT, "listMovieShowAvailability", "null", movieName, "null", 0);
        }
        builder.append(otherServer1).append(otherServer2);
        builder.append("\n=====================================\n");
        System.out.println(serverName + ">>>" + builder.toString());
        List<String> otherServ1 = new ArrayList<>();
        List<String> otherServ2 = new ArrayList<>();
        otherServ1 = Arrays.asList(otherServer1.split("@"));
        otherServ2 = Arrays.asList(otherServer2.split("@"));
        allMovieShowIDsWithCapacity.addAll(otherServ1);
        allMovieShowIDsWithCapacity.addAll(otherServ2);
        try {
            Logger.serverLog(serverID, "Admin", " CORBA List Movie Show Schedule ", "  movieName: " + movieName + " ", builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return CommonOutput.listMovieShowAvailabilityOutput(true, allMovieShowIDsWithCapacity, null);

    }

    /**
     *
     * @param customerID
     * @param movieID
     * @param movieName
     * @param numberOfTickets
     * @return
     */
    @Override
    public String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) {
        String response = "Movie Show " + movieID + " Booked Successfully";

//        if (!serverClients.containsKey(customerID)) {
//            addNewCustomerToClients(customerID);
//        }


        if (Constant.detectServer(movieID).equals(serverName)) {
            if (allMovieShows.get(movieName).containsKey(movieID)) {
                MovieModel movieSlot = allMovieShows.get(movieName).get(movieID);
                int seatsAvailable = movieSlot.getTheaterRemainCapacity();
                if(numberOfTickets>movieSlot.getTheaterRemainCapacity())
                {
                    response = "Does not have asked number seats Might Theater get Full";
                    System.out.println(serverName + ">>>" + response);
                    return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_no_capacity);
                }
                if (seatsAvailable > 0) {
                    if (seatsAvailable - numberOfTickets < 0) {
                        numberOfTickets = movieSlot.getTheaterRemainCapacity();
                    }

                    if (clientEvents.containsKey(customerID)) {
                        if (clientEvents.get(customerID).containsKey(movieName)) {
                            if (!clientEvents.get(customerID).get(movieName).containsKey(movieID)) {
                                clientEvents.get(customerID).get(movieName).put(movieID, numberOfTickets);
                            } else {
                                response = "Failed: Movieshow " + movieID + " Already Booked";
                                System.out.println(serverName + ">>>" + response);
                                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_already_booked_movieShow);
                            }
                        } else {
//                            List<String> temp = new ArrayList<>();
//                            temp.add(movieID);
                            Map<String, Integer> temp = new ConcurrentHashMap();
                            temp.put(movieID, numberOfTickets);
                            clientEvents.get(customerID).put(movieName, temp);


                        }
                    } else {
                        Map<String, Map<String, Integer>> temp = new ConcurrentHashMap<>();
//                        List<String> temp2 = new ArrayList<>();
//                        temp2.add(movieID);
                        Map<String, Integer> temp2 = new ConcurrentHashMap();
                        temp2.put(movieID, numberOfTickets);
                        temp.put(movieName, temp2);
                        clientEvents.put(customerID, temp);

                    }
                    if (movieSlot.addRegisteredClientID(customerID) == Constant.ADD_SUCCESS_FLAG) {

                        movieSlot.seatBooked(numberOfTickets);
                        allMovieShows.get(movieName).put(movieID, movieSlot);
                        response = "Success: Movie Show " + movieID + " Booked Successfully";
                        System.out.println(serverName + ">>>" + response);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.bookMovieTicketsOutput(true, CommonOutput.bookMovieShow_success_booked);
                    } else if (movieSlot.addRegisteredClientID(customerID) == Constant.SHOW_FULL_FLAG) {
                        response = "Failed: Movie Show " + movieID + " is Full";
                        System.out.println(serverName + ">>>" + response);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_no_capacity);
                    } else {
                        response = "Failed: Cannot Add You To Movie Show " + movieID;
                        System.out.println(serverName + ">>>" + response);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.bookMovieTicketsOutput(false, null);

                    }
                } else {
                    response = "Failed: Movie Show " + movieID + " is Full";
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_no_capacity);

                }
            } else {
                response = "Movie Show is not opened yet by Administration";
                System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_no_such_movieShow);
            }



        } else {

            if (!(exceedWeeklyLimit(customerID, movieID.substring(4)))) {

                String serverResponse = sendUDPMessage(Constant.getUDPServerPort(movieID.substring(0, 3)), "bookMovieShow", customerID, movieName, movieID, numberOfTickets);
                response=serverResponse;
                if (!serverResponse.startsWith("Success:")) {
                    response = "Failed: Unable to Book tickets";
                    System.out.println(serverName + ">>>" + response);
                    return CommonOutput.bookMovieTicketsOutput(false, null);

                }
            }
            else {
                response = "Failed: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_weekly_limit);

            }
        }
        try {
            Logger.serverLog(serverID, customerID, " CORBA bookMovieShow ", " movieID: " + movieID + " movieName: " + movieName + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
       return CommonOutput.bookMovieTicketsOutput(true, CommonOutput.bookMovieShow_success_booked);
    }

    /**
     *
     * @param customerID
     * @param movieID
     * @param movieName
     * @param numberOfTickets
     * @return
     */
    public String bookMovieTicketsAnotherServer(String customerID, String movieID, String movieName, int numberOfTickets) {
        String response;

        if (!(exceedWeeklyLimit(customerID, movieID.substring(4)))) {

            if (clientEvents.containsKey(customerID)) {
                if (clientEvents.get(customerID).containsKey(movieName)) {
                    clientEvents.get(customerID).get(movieName).put(movieID, numberOfTickets);
                } else {
                    Map<String, Integer> temp = new ConcurrentHashMap();
                    temp.put(movieID, numberOfTickets);
//
                    clientEvents.get(customerID).put(movieName, temp);
                }
            } else {
                Map<String, Integer> temp = new ConcurrentHashMap();
                temp.put(movieID, numberOfTickets);
                Map<String, Map<String, Integer>> temp2 = new ConcurrentHashMap();
                temp2.put(movieName, temp);
                clientEvents.put(customerID, temp2);
            }

            if (!allMovieShows.get(movieName).containsKey(movieID))
            {
                 response = "Fail: Movie Show doesn't exist";
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_no_such_movieShow);
            }

            MovieModel movieSlot = allMovieShows.get(movieName).get(movieID);

            if (movieSlot.addRegisteredClientID(customerID) == Constant.ADD_SUCCESS_FLAG) {

                movieSlot.seatBooked(numberOfTickets);
                allMovieShows.get(movieName).put(movieID, movieSlot);
                response = "Success: Movie Show " + movieID + " Booked Successfully";
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.bookMovieTicketsOutput(true, CommonOutput.bookMovieShow_success_booked);

            }
            else if(movieSlot.addRegisteredClientID(customerID)==Constant.ALREADY_BOOKED_FLAG)
            {
                response="Failed: Already registered for same movie Show";
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_already_booked_movieShow);

            }
            else
            {
                response="Failed: Unable to book";
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.bookMovieTicketsOutput(false, null);

            }

        } else {
            response = "Failed: You Cannot Book Event in Other Servers For This Week(Max Weekly Limit = 3)";
            System.out.println(serverName + ">>>" + response);
            return CommonOutput.bookMovieTicketsOutput(false, CommonOutput.bookMovieShow_fail_weekly_limit);

        }

    //}

}


    /**
     *
     * @param customerID
     * @param movieDate
     * @return
     */
    private boolean exceedWeeklyLimit(String customerID, String movieDate) {
        int limit = 0;
        if(!clientEvents.containsKey(customerID))
        {
            return false;
        }
        for (int i = 0; i < 3; i++) {
//            if(i==0 && customerID.substring(0,3).equalsIgnoreCase("ATW"))
//                continue;
//            if(i==1 && customerID.substring(0,3).equalsIgnoreCase("VER"))
//                continue;
//            if(i==2 && customerID.substring(0,3).equalsIgnoreCase("OUT"))
//                continue;
            Set<String> registeredIDs = new HashSet<>();
            switch (i) {
                case 0:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_AVTAR)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_AVTAR).keySet();
                    }
                    break;
                case 1:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_AVENGER)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_AVENGER).keySet();
                    }
                    break;
                case 2:
                    if (clientEvents.get(customerID).containsKey(Constant.MOVIE_TITANIC)) {
                        registeredIDs = clientEvents.get(customerID).get(Constant.MOVIE_TITANIC).keySet();
                    }
                    break;
            }

            for (String movieID :
                    registeredIDs) {
                if (movieID.substring(6, 8).equals(movieDate.substring(2, 4)) && movieID.substring(8, 10).equals(movieDate.substring(4, 6))) {
                    int week1 = Integer.parseInt(movieID.substring(4, 6)) / 7;
                    int week2 = Integer.parseInt(movieDate.substring(0, 2)) / 7;
//                    int diff = Math.abs(week1 - week2);
                    if (week1 == week2) {
                        limit++;
                    }
                }

                if (limit == 3)
                    return true;
            }

        }
        return false;
    }

    /**
     *
     * @param movieName
     * @return
     */
    public String listMovieShowAvailabilityUDP(String movieName)  {
        Map<String, MovieModel> movieSlots = allMovieShows.get(movieName);
        StringBuilder builder = new StringBuilder();
        builder.append(serverName + " Server, Movie Slots for" + movieName + ":\n");
        if (movieSlots.size() == 0) {
            builder.append("No Events of Type " + movieName);
        } else {
            for (MovieModel event :
                    movieSlots.values()) {
                builder.append(event.toString() + " ||\n ");
            }
        }


        builder.append("\n=====================================\n");
        return builder.toString();
    }

    /**
     *
     * @param serverPort
     * @param method
     * @param customerID
     * @param movieName
     * @param movieID
     * @param numberOfTickets
     * @return
     */
    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numberOfTickets) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID+";"+numberOfTickets;

        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);

            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);

            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return result;
    }

    /**
     *
     * @param serverPort
     * @param method
     * @param customerID
     * @param movieName
     * @param movieID
     * @param numberOfTickets
     * @param oldMovieID
     * @return
     */
    private String sendUDPMessage(int serverPort, String method, String customerID, String movieName, String movieID, int numberOfTickets, String oldMovieID) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + customerID + ";" + movieName + ";" + movieID+";"+numberOfTickets+";"+oldMovieID;

        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, serverPort);

            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);

            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }

        return result;
    }

    /**
     *
     * @param customerID
     * @return
     */
    @Override
    public String getBookingSchedule(String customerID)  {
        String response="";

        StringBuilder builder = new StringBuilder();

        String otherServer1, otherServer2;
        if (serverID.equals("ATW")) {
            otherServer1 = sendUDPMessage(Constant.VERDUN_SERVER_PORT, "listBookingSchedule",customerID , "null" ,"null", 0);
            otherServer2 = sendUDPMessage(Constant.OUTREMONT_SERVER_PORT, "listBookingSchedule", customerID, "null", "null", 0);
        } else if (serverID.equals("VER")) {
            otherServer1 = sendUDPMessage(Constant.ATWATER_SERVER_PORT, "listBookingSchedule", customerID, "null", "null", 0);
            otherServer2 = sendUDPMessage(Constant.OUTREMONT_SERVER_PORT, "listBookingSchedule", customerID, "null", "null", 0);
        } else {
            otherServer1 = sendUDPMessage(Constant.ATWATER_SERVER_PORT, "listBookingSchedule", customerID, "null", "null", 0);
            otherServer2 = sendUDPMessage(Constant.VERDUN_SERVER_PORT, "listBookingSchedule", customerID, "null", "null", 0);
        }
        builder.append(otherServer1).append(otherServer2);
        builder.append("\n"+this.serverName+":\n");
        if (clientEvents.containsKey(customerID)) {
            Map<String, Map<String,Integer>> movieShows = clientEvents.get(customerID);
            if (movieShows.size() > 0) {
                for (String movieName :
                        movieShows.keySet()) {
                    builder.append(movieName + ":\n");
                    for (String movieID :
                            movieShows.get(movieName).keySet()) {

                        //
                        builder.append(movieID + " , #ticket: "+  movieShows.get(movieName).get(movieID) + " ||\n");
                    }

                }

                builder.append("\n=====================================\n");
                response = builder.toString();
                System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.getBookingScheduleOutput(true, movieShows, null);

            }
            else
            {
                builder.append("Booking Schedule Empty For " + customerID);
                System.out.println(serverName + ">>>" + builder.toString());
                return CommonOutput.getBookingScheduleOutput(true, new HashMap<>(), null);
//            try {
//                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return response;
            }

        }
        else
        {
            builder.append("Booking Schedule Empty For " + customerID);
            System.out.println(serverName + ">>>" + builder.toString());
            return CommonOutput.getBookingScheduleOutput(true, new HashMap<>(), null);


        }
//        response = builder.toString();
//        return response;



    }

    /**
     *
     * @param customerID
     * @return
     */
    public String getBookingScheduleUDP(String customerID) throws RemoteException {
        String response="";
        StringBuilder builder = new StringBuilder();

        builder.append("\n"+this.serverName+":\n");
        if (!clientEvents.containsKey(customerID)) {
            builder.append("Booking Schedule Empty For " + customerID+"\n");
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            response = builder.toString();
//            System.out.println(serverName + ">>>" + response);
//            return CommonOutput.getBookingScheduleOutput(false, new HashMap<>(), null);
            return response;
        }
        Map<String, Map<String,Integer>> movieShows = clientEvents.get(customerID);
        if (movieShows.size() == 0) {
            builder.append("Booking Schedule Empty For " + customerID+"\n");
            try {
                Logger.serverLog(serverID, customerID, " CORBA getBookingSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            response = builder.toString();
//            System.out.println(serverName + ">>>" + response);
//            return CommonOutput.getBookingScheduleOutput(false, new HashMap<>(), null);
        }

        for (String movieName :
                movieShows.keySet()) {
            builder.append(movieName + ":\n");
            for (String movieID :
                    movieShows.get(movieName).keySet()) {

                //
                builder.append(movieID + " , #ticket: "+  movieShows.get(movieName).get(movieID) + " ||\n");
            }

        }
        response = builder.toString();
        return response;
//        System.out.println(serverName + ">>>" + response);
//
//        return CommonOutput.getBookingScheduleOutput(true, movieShows, null);

    }


    /**
     *
     * @param customerID
     * @param movieID
     * @param movieName
     * @param numberOfTickets
     * @return
     */
    @Override
    public String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets)  {
        String response;
//        System.out.println("Cancel Movie: \n Hashmap: "+ clientEvents.get(customerID).get(movieName) );
        if (Constant.detectServer(movieID).equals(serverName)) {

                if (!clientEvents.containsKey(customerID)) {


                    response = "Failed: You " + customerID + " Are Not Registered for movie " + movieName;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancel Movie Show ", " MovieId: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);
                }
                else if (!clientEvents.get(customerID).containsKey(movieName)) {
                    response = "Failed: You " + customerID + " Are Not Registered for movie " + movieName + " in Movie Show" + movieID;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA cancel Movie Show ", " MovieId: " + movieID + " movieName: " + movieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);
                }
                else {
                    if (clientEvents.get(customerID).get(movieName).containsKey(movieID)) {
                        int count = clientEvents.get(customerID).get(movieName).get(movieID);
                        if (count == numberOfTickets) {
                            clientEvents.get(customerID).get(movieName).remove(movieID);
                            allMovieShows.get(movieName).get(movieID).removeRegisteredClientID(customerID);
                        } else if (count < numberOfTickets) {

                            response = "Failed: You cannot Canceled " + numberOfTickets + " tickets. Maximum limit is " + count;
                            System.out.println(serverName + ">>>" + response);

                            try {
                                Logger.serverLog(serverID, customerID, " CORBA cancel Movie Show ", " MovieId: " + movieID + " movieName: " + movieName + " ", response);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_overcanceled_movieShow);

                        } else {
                            clientEvents.get(customerID).get(movieName).put(movieID, count - numberOfTickets);
                        }

                        MovieModel movieSlot = allMovieShows.get(movieName).get(movieID);
                        movieSlot.seatCanceled(numberOfTickets);
                        allMovieShows.get(movieName).put(movieID, movieSlot);
                        response = "Success: " + numberOfTickets + " tickets of Movie Show " + movieID + " Canceled for " + customerID;
                        System.out.println(serverName + ">>>" + response);
                        return CommonOutput.cancelMovieTicketsOutput(true, CommonOutput.cancelMovieShow_success_movieShow);

                    } else {
                        response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                        System.out.println(serverName + ">>>" + response);
                        try {
                            Logger.serverLog(serverID, customerID, " CORBA cancel Movie Show ", " MovieId: " + movieID + " movieName: " + movieName + " ", response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);
                    }


                }

        }
        else
        {

//            if (clientEvents.get(customerID).get(movieName).containsKey(movieID)) {

                 response = sendUDPMessage(Constant.getUDPServerPort(movieID.substring(0, 3)), "cancelMovieShow", customerID, movieName, movieID,numberOfTickets);
            System.out.println(serverName + ">>>" + response);

            try {
                Logger.serverLog(serverID, customerID, " CORBA cancel Movie Show ", " MovieId: " + movieID + " movieName: " + movieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);
            return response;
            //}
            //response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
        }



    }

    /**
     *
     * @param customerID
     * @param movieName
     * @param movieID
     * @return
     */
    private synchronized boolean clientHasMovieShow(String customerID, String movieName, String movieID) {
        if (clientEvents.containsKey(customerID) && clientEvents.get(customerID).containsKey(movieName)) {
            return clientEvents.get(customerID).get(movieName).containsKey(movieID);
        } else {
            return false;
        }
    }

    /**
     *
     * @param customerID
     * @param newMovieID
     * @param newMovieName
     * @param oldMovieID
     * @param numberOfTickets
     * @return
     */
    @Override
    public String exchangeTickets(String customerID, String newMovieID, String newMovieName, String oldMovieID,int numberOfTickets) {
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
        if(Constant.detectServer(oldMovieID).equals(serverName))
        {
            if (clientHasMovieShow(customerID, newMovieName, oldMovieID)) {
                String bookResp = "Failed: did not send book request for your new Movie " + newMovieID;
                String cancelResp = "Failed: did not send cancel request for your old Movie " + oldMovieID;
                synchronized (this) {
                    if (Constant.onTheSameWeek(newMovieID.substring(4), oldMovieID) && !exceedWeeklyLimit(customerID, newMovieID.substring(4))) {
                        cancelResp = cancelMovieTickets(customerID, oldMovieID, newMovieName, numberOfTickets);

                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookMovieTickets(customerID, newMovieID, newMovieName,numberOfTickets);

                        }
                    } else {
                        bookResp = bookMovieTickets(customerID, newMovieID, newMovieName,numberOfTickets);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelMovieTickets(customerID, oldMovieID, newMovieName,numberOfTickets);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Event " + oldMovieID + " swapped with " + newMovieID;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + newMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.exchangeTicketsOutput(true,null);
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    bookMovieTickets(customerID, newMovieID, newMovieName,numberOfTickets);
                    response = "Failed: Your oldMovieShow " + oldMovieID + " Could not be Canceled reason: " + cancelResp;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + newMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.exchangeTicketsOutput(false,null);
                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    //hope this won't happen, but just in case.
                    String resp1 = bookMovieTickets(customerID, oldMovieID, newMovieName,numberOfTickets);
                    response = "Failed: Your newEvent " + newMovieID + " Could not be Booked reason: " + bookResp + " And your oldMovieID Rolling back: " + resp1;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + newMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.exchangeTicketsOutput(false,null);
                } else {
                    response = "Failed: on Both newEvent " + newMovieID + " Booking reason: " + bookResp + " and oldMovieID " + oldMovieID + " Canceling reason: " + cancelResp;
                    System.out.println(serverName + ">>>" + response);
                    try {
                        Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + newMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return CommonOutput.exchangeTicketsOutput(false,null);
                }


            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
                System.out.println(serverName + ">>>" + response);
                try {
                    Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " oldMovieID: " + oldMovieID + " oldMovieName: " + newMovieName + " newMovieID: " + newMovieID + " newMovieName: " + newMovieName + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return CommonOutput.exchangeTicketsOutput(false,CommonOutput.exchangeTicket_fail_not_registered_in_movieShow);
            }
        }
        else
        {
            response = sendUDPMessage(Constant.getUDPServerPort(oldMovieID.substring(0, 3)), "exchangeTickets", customerID, newMovieName, newMovieID,numberOfTickets,oldMovieID);
            System.out.println(serverName + ">>>" + response);
            try {
                Logger.serverLog(serverID, customerID, " CORBA exchangeTickets ", " MovieId: " + newMovieID + " movieName: " + newMovieName + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return CommonOutput.exchangeTicketsOutput(false,null);
        }

        //}
    }

    /**
     *
     */
    @Override
    public void shutdown() {
        orb.shutdown(false);
    }

    /**
     *
     * @param customerID
     * @param movieID
     * @param movieName
     * @param numberOfTickets
     * @return
     * @throws RemoteException
     */
    public String cancelMovieTicketsUDP(String customerID, String movieID, String movieName, int numberOfTickets)throws RemoteException
    {
        String response;

        if (!clientEvents.containsKey(customerID)) {

            response = "Failed: You " + customerID + " Are Not Registered for movie " + movieName;
            System.out.println(serverName + ">>>" + response);

            //                    try {
//                        Logger.serverLog(serverID, customerID, " CORBA cancelEvent ", " eventID: " + eventID + " eventType: " + eventType + " ", response);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
            return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

        } else if (!clientEvents.get(customerID).containsKey(movieName)) {
            response = "Failed: You " + customerID + " Are Not Registered for movie " + movieName + " in Movie Show" + movieID;
            System.out.println(serverName + ">>>" + response);

            return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);

        } else {
            if (clientEvents.get(customerID).get(movieName).containsKey(movieID)) {
                int count = clientEvents.get(customerID).get(movieName).get(movieID);
                if (count == numberOfTickets) {
                    clientEvents.get(customerID).get(movieName).remove(movieID);
                    allMovieShows.get(movieName).get(movieID).removeRegisteredClientID(customerID);
                } else if (count < numberOfTickets) {
                    response= "Failed: You cannot Canceled " + numberOfTickets + " tickets. Maximum limit is " + count;
                    System.out.println(serverName + ">>>" + response);
                    return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_overcanceled_movieShow);

                } else {
                    clientEvents.get(customerID).get(movieName).put(movieID, count - numberOfTickets);
                }

                MovieModel movieSlot = allMovieShows.get(movieName).get(movieID);
                movieSlot.seatCanceled(numberOfTickets);
                allMovieShows.get(movieName).put(movieID, movieSlot);
                response = "Success: " + numberOfTickets + " tickets of Movie Show " + movieID + " Canceled for " + customerID;
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.cancelMovieTicketsOutput(true, CommonOutput.cancelMovieShow_success_movieShow);


            } else {
                response = "Failed: You " + customerID + " Are Not Registered in " + movieID;
                System.out.println(serverName + ">>>" + response);
                return CommonOutput.cancelMovieTicketsOutput(false, CommonOutput.cancelMovieShow_fail_not_registered_in_movieShow);


            }
        }

    }


    /**
     *
     * @param oldMovieID
     * @param movieName
     * @param customerID
     * @param numberOfTickets
     * @return
     * @throws RemoteException
     */
    public String removeEventUDP(String oldMovieID, String movieName, String customerID,int numberOfTickets) throws RemoteException {
        if (!clientEvents.containsKey(customerID)) {

            return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
        }else if(!clientEvents.get(customerID).containsKey(movieName))
        {
            return "Failed: You " + customerID + " Are Not Registered in movie" + movieName;
        }
        else {
            if (clientEvents.get(customerID).get(movieName).containsKey(oldMovieID)) {


                        MovieModel movieSlot =allMovieShows.get(movieName).get(oldMovieID);
                        int count = clientEvents.get(customerID).get(movieName).remove(oldMovieID);
                        movieSlot.seatCanceled(count);
                        movieSlot.removeRegisteredClientID(customerID);
                        allMovieShows.get(movieName).put(oldMovieID,movieSlot);

                return "Success: Movie " + oldMovieID + " Was Removed from " + customerID + " Schedule";
            } else {
                return "Failed: You " + customerID + " Are Not Registered in " + oldMovieID;
            }
       }
    }
}
