package Client;

/**
 * author: Naren Zadafiya (40232646)
 */

import Constant.Constant;
import Client.Log.ClientLogger;
import ServerObjectInterfaceApp.ServerObjectInterface;
import ServerObjectInterfaceApp.ServerObjectInterfaceHelper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.util.Scanner;

public class Client {
    static Scanner sc;
    public static void main(String args[]) throws Exception {
        System.out.println("\n|===========================================================|");
        System.out.println("| Welcome To DMTBS: Distributed Movie Ticket Booking System |");
        System.out.println("|===========================================================|");
        System.out.println("\nSystem is made by:\n");
        System.out.println("\t\t\t Naren Zadafiya (40232646)\n\n");

        NamingContextExt ncRef = null;
        try {
            ORB orb = ORB.init(args, null);
            // -ORBInitialPort 1050 -ORBInitialHost localhost
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            ncRef = NamingContextExtHelper.narrow(objRef);
            init(ncRef);
        } catch (Exception e) {
            System.out.println("Client ORB init exception: " + e);
            e.printStackTrace();
        }

        init(ncRef);
    }

    /**
     *
     * @param ncRef
     * @throws Exception
     */
    public static void init(NamingContextExt ncRef) throws Exception
    {
        sc=new Scanner(System.in);
        String userID;

        System.out.println("*************************************");
        System.out.println("*************************************");
        System.out.println("Please Enter your UserID(For Concurrency test enter 'ConTest'):");
        userID = sc.next().trim().toUpperCase();
//        if (userID.equalsIgnoreCase("ConTest")) {
//            startConcurrencyTest(ncRef);
//        } else {
//            System.out.println("Please Enter your UserID:");
//            userID = sc.next().trim().toUpperCase();
            switch (Constant.checkUserType(userID)) {
                case Constant.USER_TYPE_CUSTOMER:
                    try {
                        System.out.println("Customer Login successful (" + userID + ")");
                        ClientLogger.clientLog(userID, " Customer Login successful");
                        customer(userID, Constant.getServerPort(userID.substring(0, 3)), ncRef);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case Constant.USER_TYPE_ADMIN:
                    try {
                        System.out.println("Admin Login successful (" + userID + ")");
                        ClientLogger.clientLog(userID, " Admin Login successful");
                        admin(userID, Constant.getServerPort(userID.substring(0, 3)), ncRef);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    System.out.println("!!UserID is not in correct format");

                    init(ncRef);
            }
//        }




    }

    /**
     *
     * @param ncRef
     * @throws Exception
     */
//    private static void startConcurrencyTest(NamingContextExt ncRef) throws Exception {
//        System.out.println("Concurrency Test Starting for Book Movie Show");
//        System.out.println("Connecting Atwater Server...");
//        String movieName = Constant.MOVIE_AVTAR;
//        String movieID = "ATWA260223";
//        int numberOfTickets=2;
//        ServerObjectInterface servant = ServerObjectInterfaceHelper.narrow(ncRef.resolve_str("ATW"));
//        System.out.println("adding " + movieID + " " + movieName + " with capacity 2 to Atwater Server...");
//        String response = servant.addMovieSlot(movieID, movieName, 2);
//        System.out.println(response);
//        Runnable task1 = () -> {
//            String customerID = "ATWC1234";
////            System.out.println("Connecting Montreal Server for " + customerID)
//
//            String res = servant.bookMovieTickets(customerID, movieID, movieName,numberOfTickets);
//            System.out.println("Booking response for " + customerID + " " + res);
//            res = servant.cancelMovieTickets(customerID, movieID, movieName,numberOfTickets);
//            System.out.println("Canceling response for " + customerID + " " + res);
//        };
//        Runnable task2 = () -> {
//            String customerID = "ATWC3456";
////            System.out.println("Connecting Montreal Server for " + customerID);
//            String res = servant.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Booking response for " + customerID + " " + res);
//            res = servant.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Canceling response for " + customerID + " " + res);
//        };
//        Runnable task3 = () -> {
//            String customerID = "ATWC4567";
////            System.out.println("Connecting Montreal Server for " + customerID);
//            String res = servant.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Booking response for " + customerID + " " + res);
//            res = servant.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Canceling response for " + customerID + " " + res);
//        };
//        Runnable task4 = () -> {
//            String customerID = "ATWC6789";
////            System.out.println("Connecting Montreal Server for " + customerID);
//            String res = servant.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Booking response for " + customerID + " " + res);
//            res = servant.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Canceling response for " + customerID + " " + res);
//        };
//        Runnable task5 = () -> {
//            String customerID = "ATWC7890";
////            System.out.println("Connecting Montreal Server for " + customerID);
//            String res = servant.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Booking response for " + customerID + " " + res);
//            res = servant.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
//            System.out.println("Canceling response for " + customerID + " " + res);
//        };
//
//        Runnable task6 = () -> {
////            System.out.println("Connecting Montreal Server for " + customerID);
//            String res = servant.removeMovieSlots(movieID, movieName);
//            System.out.println("removeEvent response for " + movieID + " " + res);
//        };
//
//        Thread thread1 = new Thread(task1);
//        Thread thread2 = new Thread(task2);
//        Thread thread3 = new Thread(task3);
//        Thread thread4 = new Thread(task4);
//        Thread thread5 = new Thread(task5);
//        Thread thread6 = new Thread(task6);
////        synchronized (thread1) {
//        thread1.start();
//        thread2.start();
//        thread3.start();
//        thread4.start();
//        thread5.start();
////        }
//        thread1.join();
//        thread2.join();
//        thread3.join();
//        thread4.join();
//        thread5.join();
////        if (!thread1.isAlive() && !thread2.isAlive() && !thread3.isAlive() && !thread4.isAlive() && !thread5.isAlive()) {
//        System.out.println("Concurrency Test Finished for BookMovie Show");
//        thread6.start();
//        thread6.join();
//        init(ncRef);
////        }
//    }

    /**
     *
     * @param customerID
     * @param serverPort
     * @param ncRef
     * @throws Exception
     */
    private static void customer(String customerID, int serverPort,NamingContextExt ncRef) throws Exception{
        String serverID = Constant.getServerID(customerID);
        if (serverPort == 1) {
            return;
        }
//        Registry registry = LocateRegistry.getRegistry(serverPort);
//        MovieManagementInterface remoteObject = (MovieManagementInterface) registry.lookup(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME);
        ServerObjectInterface servant = ServerObjectInterfaceHelper.narrow(ncRef.resolve_str(serverID));

        boolean repeat = true;
        Constant.printMenu(Constant.USER_TYPE_CUSTOMER);
        int menuSelection = sc.nextInt();
        String movieName;
        String movieID;
        String serverResponse;
        int numberOfTickets;

        switch (menuSelection) {
            case Constant.CUSTOMER_BOOK_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                ClientLogger.clientLog(customerID, " attempting to book movie for "+ movieName+" with movieId "+ movieID+" and number of Tickets : "+numberOfTickets);
                serverResponse = servant.bookMovieTickets(customerID, movieID, movieName, numberOfTickets);
                ClientLogger.clientLog(customerID, " bookMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);
                System.out.println(serverResponse);
                break;
            }
            case Constant.CUSTOMER_CANCEL_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                ClientLogger.clientLog(customerID, " attempting to cancel movie for "+ movieName+" with movieId "+ movieID+" and number of Tickets : "+numberOfTickets);
                serverResponse = servant.cancelMovieTickets(customerID, movieID, movieName, numberOfTickets);
                System.out.println(serverResponse);
                ClientLogger.clientLog(customerID, " cancelMovieShow", " movieID: " + movieID + " movieName: " + movieName +" numberOfTickets: "+numberOfTickets + " ", serverResponse);

                break;
            }
            case Constant.CUSTOMER_GET_BOOKING_SCHEDULE:
            {
                ClientLogger.clientLog(customerID, " attempting to get Movie Schedule");
                serverResponse = servant.getBookingSchedule(customerID);

                System.out.println(serverResponse);
                ClientLogger.clientLog(customerID, " getBookingSchedule", " null ", serverResponse);
                break;
            }
            case Constant.CUSTOMER_EXCHANGE_TICKET:
            {
                System.out.println("Please Enter the OLD Movie Show to be replaced");
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);

                System.out.println("Please Enter the NEW movie Show to be replaced");

                String newMovieID = Constant.promptForMovieID(sc);
                numberOfTickets = Constant.promptForTicketsCount(sc);
                ClientLogger.clientLog(customerID, " attempting to Exchange Ticket");

                serverResponse = servant.exchangeTickets(customerID, newMovieID, movieName, movieID, numberOfTickets);
                System.out.println(serverResponse);
                ClientLogger.clientLog(customerID, " Exchange Movie Ticket", " old movieID: " + movieID + " movieName: " + movieName + " new MovieID: " + newMovieID  + " ", serverResponse);
                break;
            }
            case Constant.CUSTOMER_LOGOUT:
            {
                System.out.println(customerID + " Logout Successfully!!");
                ClientLogger.clientLog(customerID, " Logout Successfully");
                repeat=false;
                init(ncRef);
                break;
            }
            case Constant.SHUTDOWN:
                ClientLogger.clientLog(customerID, " attempting ORB shutdown");
                servant.shutdown();
                ClientLogger.clientLog(customerID, " shutdown");
                return;

            default:
                System.out.println("Please Enter Valid Choice.");
        }
        if(repeat)
        {
            customer(customerID,serverPort,ncRef);

        }
    }

    /**
     *
     * @param adminID
     * @param serverPort
     * @param ncRef
     * @throws Exception
     */
    private static void admin(String adminID, int serverPort,NamingContextExt ncRef) throws Exception{
        String serverID = Constant.getServerID(adminID);
        if (serverPort == 1) {
            return;
        }
//        Registry registry = LocateRegistry.getRegistry(serverPort);
//        MovieManagementInterface remoteObject = (MovieManagementInterface) registry.lookup(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME);
        ServerObjectInterface servant = ServerObjectInterfaceHelper.narrow(ncRef.resolve_str(serverID));
        boolean repeat = true;
        Constant.printMenu(Constant.USER_TYPE_ADMIN);

        String movieName;
        String movieID;
        String serverResponse;
        int bookingCapacity;
        int menuSelection = sc.nextInt();
        switch(menuSelection)
        {
            case Constant.ADMIN_ADD_MOVIE: {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                if(!Constant.isMovieDateWithinOneWeek(movieID.substring(4))){
                    System.out.println("You Can enter movie show for this week only!");
                    break;
                }
                bookingCapacity = Constant.promptForCapacity(sc);
                ClientLogger.clientLog(adminID, " attempting to Add Slot for "+ movieName+" with movieId "+ movieID+" and capacity: "+bookingCapacity);
                serverResponse = servant.addMovieSlot(adminID,movieID,movieName,bookingCapacity);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " addMovieSlot", " movieID: " + movieID + " movieName: " + movieName +" capacity: "+bookingCapacity+ " ", serverResponse);

                break;
            }
            case Constant.ADMIN_REMOVE_MOVIE:
            {
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                ClientLogger.clientLog(adminID, " attempting to remove movie slot for "+ movieName+" with movieId"+ movieID);

                serverResponse = servant.removeMovieSlots(adminID,movieID,movieName);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " removeMovieSlot", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_LIST_MOVIE_AVAILABILITY:{
                movieName = Constant.promptForMovieName(sc);
                ClientLogger.clientLog(adminID, " attempting to list schedule of movie for "+ movieName);

                serverResponse = servant.listMovieShowAvailability(adminID,movieName);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " listMovieShowAvailability", " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_BOOK_MOVIE:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForTicketsCount(sc);
                ClientLogger.clientLog(adminID, " attempting to book Slot for "+ movieName+" with movieId"+ movieID+" and capacity: "+bookingCapacity);
                serverResponse = servant.bookMovieTickets(adminID, movieID, movieName, bookingCapacity);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " bookMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_CANCEL_MOVIE:{
                movieName = Constant.promptForMovieName(sc);
                movieID = Constant.promptForMovieID(sc);
                bookingCapacity = Constant.promptForCapacity(sc);
                ClientLogger.clientLog(adminID, " attempting to cancel movie seats for "+ movieName+" with movieId"+ movieID+" and capacity: "+bookingCapacity);

                serverResponse = servant.cancelMovieTickets(adminID, movieID, movieName, bookingCapacity);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " cancelMovieShow", " movieID: " + movieID + " movieName: " + movieName + " ", serverResponse);

                break;
            }
            case Constant.ADMIN_GET_BOOKING_SCHEDULE:{
                ClientLogger.clientLog(adminID, " attempting to get Movie Schedule");
                serverResponse = servant.getBookingSchedule(adminID);
                System.out.println(serverResponse);
                ClientLogger.clientLog(adminID, " getBookingSchedule", " null ", serverResponse);

                break;
            }

            case Constant.ADMIN_LOGOUT:{
                repeat=false;
                System.out.println(adminID + " Logout Successfully!!");
                ClientLogger.clientLog(adminID, " Logout Successfully");
                init(ncRef);
                break;
            }
            case Constant.SHUTDOWN:
                ClientLogger.clientLog(adminID, " attempting ORB shutdown");
                servant.shutdown();
                ClientLogger.clientLog(adminID, " shutdown");
                return;
            default:
            {
                System.out.println("Please Enter valid Choice");
            }

        }
        if (repeat) {
            admin(adminID, serverPort, ncRef);
        }
    }

}


