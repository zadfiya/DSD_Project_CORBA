package Replica4;



import Replica4.*;
import Replica4.DMTBSsystem.DMTBS;
import Replica4.DMTBSsystem.DMTBSHelper;
import Replica4.Server.Server;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;



public class RM4 {
    public static int lastSequenceID = 1;
    public static ConcurrentHashMap<Integer, Message> message_list = new ConcurrentHashMap<>();
    public static Queue<Message> message_q = new ConcurrentLinkedQueue<Message>();
    private static boolean serversFlag = true;

    public static void main(String[] args) throws Exception {
        NamingContextExt ncRef = null;
        try {
            ORB orb = ORB.init(args, null);
            // -ORBInitialPort 1050 -ORBInitialHost localhost
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            ncRef = NamingContextExtHelper.narrow(objRef);
            Run(ncRef);
            //init(ncRef);
        } catch (Exception e) {
            System.out.println("Client ORB init exception: " + e);
            e.printStackTrace();
        }

    }

    private static void Run(NamingContextExt ncRef) throws Exception {
        Runnable task = () -> {
            try {
                receive(ncRef);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    private static void receive(NamingContextExt ncRef) throws Exception {
        MulticastSocket socket = null;
        try {

            socket = new MulticastSocket(1234);

            socket.joinGroup(InetAddress.getByName("230.1.1.10"));

            byte[] buffer = new byte[1000];
            System.out.println("RM1 UDP Server Started(port=1234)............");

            //Run thread for executing all messages in queue
            Runnable task = () -> {
                try {
                    executeAllRequests(ncRef);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread = new Thread(task);
            thread.start();

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String data = new String(request.getData(), 0, request.getLength());
                String[] parts = data.split(";");

                /*
                Message Types:
                    00- Simple message
                    01- Sync request between the RMs
                    02- Initialing RM
                    11-Rm1 has bug
                    12-RM1 has bug
                    13-Rm3 has bug
                    21-Rm1 is down
                    22-RM1 is down
                    23-Rm3 is down
                */
                System.out.println("RM1 recieved message. Detail:" + data);
                if (parts[2].equalsIgnoreCase("00")) {
                    Message message = message_obj_create(data);
                    Message message_To_RMs = message_obj_create(data);
                    message_To_RMs.MessageType = "01";
                    send_multicast_toRM(message_To_RMs);
                    if (message.sequenceId - lastSequenceID > 1) {
                        Message initial_message = new Message(0, "Null", "02", Integer.toString(lastSequenceID), Integer.toString(message.sequenceId), "RM1", "Null", "Null", 0, 0);
                        System.out.println("RM1 send request to update its message list. from:" + lastSequenceID + "To:" + message.sequenceId);
                        // Request all RMs to send back list of messages
                        send_multicast_toRM(initial_message);
                    }
                    System.out.println("is adding queue:" + message);
                    message_q.add(message);
                    message_list.put(message.sequenceId, message);
                } else if (parts[2].equalsIgnoreCase("01")) {
                    Message message = message_obj_create(data);
                    if (!message_list.contains(message.sequenceId))
                        message_list.put(message.sequenceId, message);
                } else if (parts[2].equalsIgnoreCase("02")) {
                    initial_send_list(Integer.parseInt(parts[3]), Integer.parseInt(parts[4]), parts[5]);
                } else if (parts[2].equalsIgnoreCase("03") && parts[5].equalsIgnoreCase("RM1")) {
                    update_message_list(parts[1]);
                } else if (parts[2].equalsIgnoreCase("11")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM1 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("12")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM1 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("13")) {
                    Message message = message_obj_create(data);
                    System.out.println("RM1 has bug:" + message.toString());
                } else if (parts[2].equalsIgnoreCase("21")) {
                    Runnable crash_task = () -> {
                        try {
<<<<<<< HEAD
                            //suspend the execution of messages untill all servers are up. (serversFlag=false)
                            serversFlag = false;
                            //reboot Monteal Server
                            //Registry montreal_registry = LocateRegistry.getRegistry(SERVER_MONTREAL);
                            DMTBS atwater_obj = DMTBSHelper.narrow(ncRef.resolve_str("ATW"));
                            //EventManagementInterface montreal_obj = (EventManagementInterface) montreal_registry.lookup(EVENT_MANAGEMENT_REGISTERED_NAME);
                            atwater_obj.shutdown();
//                            Montreal.main(new String[0]);
                            System.out.println("RM1 shutdown Atwater Server");

                            //reboot Quebec Server
//                            Registry quebec_registry = LocateRegistry.getRegistry(SERVER_QUEBEC);
//                            EventManagementInterface quebec_obj = (EventManagementInterface) quebec_registry.lookup(EVENT_MANAGEMENT_REGISTERED_NAME);
//                            quebec_obj.shutDown();
//                            Quebec.main(new String[0]);
=======
                            serversFlag = false;
                            DMTBS atwater_obj = DMTBSHelper.narrow(ncRef.resolve_str("ATW"));
                            atwater_obj.shutdown();
                            System.out.println("RM1 shutdown Atwater Server");

>>>>>>> e28ea3b83a4da5e29f3fe9faa369f964e1bb86b0
                            DMTBS verdun_obj = DMTBSHelper.narrow(ncRef.resolve_str("VER"));
                            verdun_obj.shutdown();
                            System.out.println("RM1 shutdown Verdun Server");

<<<<<<< HEAD
                            //reboot Sherbrooke Server
//                            Registry sherbrook_registry = LocateRegistry.getRegistry(SERVER_SHERBROOKE);
//                            EventManagementInterface sherbrook_obj = (EventManagementInterface) sherbrook_registry.lookup(EVENT_MANAGEMENT_REGISTERED_NAME);
//                            sherbrook_obj.shutDown();
                            DMTBS outremont_obj = DMTBSHelper.narrow(ncRef.resolve_str("OUT"));
                            outremont_obj.shutdown();
//                            Sherbrooke.main(new String[0]);
                            System.out.println("RM1 shutdown Outremont Server");

                            //This is going to start all the servers for this implementation
                            Server.main(new String[0]);

                            //wait untill are servers are up
=======
                            DMTBS outremont_obj = DMTBSHelper.narrow(ncRef.resolve_str("OUT"));
                            outremont_obj.shutdown();
                            System.out.println("RM1 shutdown Outremont Server");

                            Server.main(new String[0]);

>>>>>>> e28ea3b83a4da5e29f3fe9faa369f964e1bb86b0
                            Thread.sleep(5000);

                            System.out.println("RM1 is reloading servers hashmap");
                            reloadServers(ncRef);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    };
                    Thread handleThread = new Thread(crash_task);
                    handleThread.start();
                    System.out.println("RM1 handled the crash!");
                    serversFlag = true;
                }
            }

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (socket != null)
                socket.close();
        }
    }

    private static Message message_obj_create(String data) {
        String[] parts = data.split(";");
        int sequenceId = Integer.parseInt(parts[0]);
        String FrontIpAddress = parts[1];
        String MessageType = parts[2];
        String Function = parts[3];
        String userID = parts[4];
        String newMovieID = parts[5];
        String newMovieName = parts[6];
        String oldMovieID = parts[7];
        int numberOfTickets = Integer.parseInt(parts[8]);
        int bookingCapacity = Integer.parseInt(parts[9]);
        Message message = new Message(sequenceId, FrontIpAddress, MessageType, Function, userID, newMovieID, newMovieName, oldMovieID, numberOfTickets, bookingCapacity);
        return message;
    }

    // Create a list of messsages, seperating them with @ and send it back to RM
    private static void initial_send_list(Integer begin, Integer end, String RmNumber) {
        String list = "";
        for (ConcurrentHashMap.Entry<Integer, Message> entry : message_list.entrySet()) {
            if (entry.getValue().sequenceId > begin && entry.getValue().sequenceId < end) {
                list += entry.getValue().toString() + "@";
            }
        }
        // Remove the last @ character
        if (list.endsWith("@"))
            list.substring(list.length() - 1);
        Message message = new Message(0, list, "03", begin.toString(), end.toString(), RmNumber, "Null", "Null", 0, 0);
        System.out.println("RM1 sending its list of messages for initialization. list of messages:" + list);
        send_multicast_toRM(message);
    }

    //update the hasmap and and new data to queue to be execited
    private static void update_message_list(String data) {
        String[] parts = data.split("@");
        for (int i = 0; i < parts.length; ++i) {
            Message message = message_obj_create(parts[i]);
            //we get the list from 2 other RMs and will ensure that there will be no duplication
            if (!message_list.containsKey(message.sequenceId)) {
                System.out.println("RM1 update its message list" + message);
                message_q.add(message);
                message_list.put(message.sequenceId, message);
            }
        }
    }

    private static void send_multicast_toRM(Message message) {
        int port = 1234;
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            byte[] data = message.toString().getBytes();
            InetAddress aHost = InetAddress.getByName("230.1.1.10");

            DatagramPacket request = new DatagramPacket(data, data.length, aHost, port);
            socket.send(request);
            System.out.println("Message multicasted from RM1 to other RMs. Detail:" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Execute all request from the lastSequenceID, send the response back to Front and update the counter(lastSequenceID)
    private static void executeAllRequests(NamingContextExt ncRef) throws Exception {
        System.out.println("before while true");
        while (true) {
            synchronized (RM4.class) {
                Iterator<Message> itr = message_q.iterator();
                while (itr.hasNext()) {
                    Message data = itr.next();
                    System.out.println("RM1 is executing message request. Detail:" + data);
                    //when the servers are down serversFlag is False therefore, no execution untill all servers are up.
                    if (data.sequenceId == lastSequenceID && serversFlag) {
                        System.out.println("RM1 is executing message request. Detail:" + data);
                        String response = requestToServers(data,ncRef);
                        Message message = new Message(data.sequenceId, response, "RM1",
                                data.Function, data.userID, data.newMovieID,
                                data.newMovieType, data.oldMovieID,
                                data.numberOfTickets, data.bookingCapacity);
                        lastSequenceID += 1;
                        messsageToFront(message.toString(), data.FrontIpAddress);
                        message_q.poll();
                    }
                }
//                message_q.clear();
            }
        }
    }

<<<<<<< HEAD
    //Send RMI request to server
    private static String requestToServers(Message input,NamingContextExt ncRef) throws Exception {
        String serverID = getServerID(input.userID.substring(0, 3));
//        Registry registry = LocateRegistry.getRegistry(portNumber);
//        EventManagementInterface obj = (EventManagementInterface) registry.lookup(EVENT_MANAGEMENT_REGISTERED_NAME);
=======
    //Send Corba request to server
    private static String requestToServers(Message input,NamingContextExt ncRef) throws Exception {
        String serverID = getServerID(input.userID.substring(0, 3));
>>>>>>> e28ea3b83a4da5e29f3fe9faa369f964e1bb86b0
        DMTBS obj = DMTBSHelper.narrow(ncRef.resolve_str(serverID));

        if (input.userID.substring(3, 4).equalsIgnoreCase("A")) {
            if (input.Function.equalsIgnoreCase("addMovieSlot")) {
                String response = obj.addMovieSlots(input.newMovieID, input.newMovieType, input.bookingCapacity);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("removeMovieSlot")) {
                String response = obj.removeMovieSlots(input.newMovieID, input.newMovieType);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("listMovieShowAvailability")) {
                String response = obj.listMovieShowsAvailability(input.newMovieType);
                System.out.println(response);
                return response;
            }
        } else if (input.userID.substring(3, 4).equalsIgnoreCase("C")) {
            if (input.Function.equalsIgnoreCase("bookMovieTickets")) {
                String response = obj.bookMovieTickets(input.userID, input.newMovieID, input.newMovieType,input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("getBookingSchedule")) {
                String response = obj.getBookingSchedule(input.userID);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("cancelMovieTickets")) {
                String response = obj.cancelMovieTickets(input.userID, input.newMovieID, input.newMovieType, input.numberOfTickets);
                System.out.println(response);
                return response;
            } else if (input.Function.equalsIgnoreCase("exchangeTickets")) {
                String response = obj.exchangeTickets(input.userID, input.newMovieID, input.newMovieType, input.oldMovieID, input.numberOfTickets);
                System.out.println(response);
                return response;
            }
        }
        return "Null response from server" + input.userID.substring(0, 3);
    }

    private static int serverPort(String input) {
        String branch = input.substring(0, 3);
        int portNumber = -1;

        if (branch.equalsIgnoreCase("ATW"))
            portNumber = 2964;
        else if (branch.equalsIgnoreCase("OUT"))
            portNumber = 2965;
        else if (branch.equalsIgnoreCase("VER"))
            portNumber = 2966;

        return portNumber;
    }
    public static String getServerID(String userID) {
        String branchAcronym = userID.substring(0, 3).toUpperCase();
        if (branchAcronym.equalsIgnoreCase("ATW")) {
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("VER")) {
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("OUT")) {
            return branchAcronym;
        }
        return "1";
    }

    public static void messsageToFront(String message, String FrontIpAddress) {
        System.out.println("Message to front:" + message);
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(4321);
            byte[] bytes = message.getBytes();
            InetAddress aHost = InetAddress.getByName(FrontIpAddress);

            System.out.println(aHost);
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, aHost, 1999);
            socket.send(request);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

    }

    public static void reloadServers(NamingContextExt ncRef) throws Exception {
        for (ConcurrentHashMap.Entry<Integer, Replica4.Message> entry : message_list.entrySet()) {
            if (entry.getValue().sequenceId < lastSequenceID)
                requestToServers(entry.getValue(),ncRef);
        }
    }
}
