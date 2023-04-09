package Replica2; /**
 * author: Naren Zadafiya (40232646)
 */

import Replica2.ServerObjectInterfaceApp2.ServerObjectInterface2;
import Replica2.ServerObjectInterfaceApp2.ServerObjectInterface2Helper;
import org.omg.CORBA.ORB;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.rmi.RemoteException;

public class ServerInstance {

    private String serverID;
    private String serverName;
    private int serverRegistryPort;
    private int serverUdpPort;

    /**
     *
     * @param serverID
     * @param args
     * @throws Exception
     */
    public ServerInstance(String serverID,  String[] args)  throws Exception {
        this.serverID = serverID;
        switch (serverID) {
            case "ATW":
                serverName = Constant.SERVER_ATWATER;
                serverRegistryPort = Constant.ATWATER_REGISTRY_PORT;
                serverUdpPort = Constant.ATWATER_SERVER_PORT;
                break;
            case "VER":
                serverName = Constant.SERVER_VERDUN;
                serverRegistryPort = Constant.VERDUN_REGISTRY_PORT;
                serverUdpPort = Constant.VERDUN_SERVER_PORT;
                break;
            case "OUT":
                serverName = Constant.SERVER_OUTREMONT;
                serverRegistryPort = Constant.OUTREMONT_REGISTRY_PORT;
                serverUdpPort = Constant.OUTREMONT_SERVER_PORT;
                break;
        }

        // create and initialize the ORB //// get reference to rootpoa &amp; activate
        // the POAManager
        ORB orb = ORB.init(args, null);
        // -ORBInitialPort 1050 -ORBInitialHost localhost
        POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
        rootpoa.the_POAManager().activate();

        // create servant and register it with the ORB
        ServerImplement servant = new ServerImplement(serverID, serverName);
        servant.setORB(orb);

        // get object reference from the servant
        org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
        ServerObjectInterface2 href = ServerObjectInterface2Helper.narrow(ref);

        org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
        NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

        NameComponent[] path = ncRef.to_name(serverID);
        ncRef.rebind(path, href);

//        ServerImplement remoteObject = new ServerImplement(serverID, this.serverName);
//        MovieManagementInterface obj = (MovieManagementInterface) UnicastRemoteObject.exportObject(remoteObject,0);
//        Registry registry = LocateRegistry.createRegistry(this.serverRegistryPort);
//        registry.bind(Constant.MOVIE_MANAGEMENT_REGISTERED_NAME, obj);
        System.out.println(this.serverName + " Server is Up & Running");
        Logger.serverLog(serverID, " Server is Up & Running");
        //this.addTestData(remoteObject);
        Runnable task = () -> {
            listenForRequest(servant, this.serverUdpPort, this.serverName, serverID);
        };
        Thread thread = new Thread(task);
        thread.start();
    }

    /**
     *
     * @param remoteObject
     * @throws RemoteException
     */
    private void addTestData(ServerImplement remoteObject) throws RemoteException {
        switch (serverID) {
            case "ATW":
                remoteObject.addMovieSlot("ATWA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("ATWM100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("ATWE100223", Constant.MOVIE_AVTAR, 20);
                break;
            case "VER":
                remoteObject.addMovieSlot("VERA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("VERM100223", Constant.MOVIE_AVTAR, 20);
                 remoteObject.addMovieSlot("VERE100223", Constant.MOVIE_AVTAR, 20);

                break;
            case "OUT":
                remoteObject.addMovieSlot("OUTA100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("OUTM100223", Constant.MOVIE_AVTAR, 20);
                remoteObject.addMovieSlot("OUTE100223", Constant.MOVIE_AVTAR, 20);
                break;
        }
    }

    /**
     *
     * @param obj
     * @param serverUdpPort
     * @param serverName
     * @param serverID
     */
    private static void listenForRequest(ServerImplement obj, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());

                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieName = parts[2];
                String movieID = parts[3];
                int numberOfTickets = Integer.parseInt(parts[4]);
//                String oldMovieName=null;
                String oldMovieID=null;

                if(parts.length>5)
                {

                     oldMovieID = parts[5];
                }

                if (method.equalsIgnoreCase("removeMovieSlot")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", " ...");

                    String result = obj.bookMovieTickets(customerID,movieID, movieName,numberOfTickets);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listMovieShowAvailability")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieName: " + movieName + " ", " ...");
                    String result = obj.listMovieShowAvailabilityUDP(movieName);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookMovieShow")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", " ...");
                    String result = obj.bookMovieTicketsAnotherServer(customerID, movieID, movieName, numberOfTickets);
                    sendingResult = result + ";";
                }else if(method.equalsIgnoreCase("listBookingSchedule")){


                    String result = obj.getBookingScheduleUDP(customerID);

                    sendingResult = result + ";";
                }
                else if (method.equalsIgnoreCase("cancelMovieShow")) {
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", " ...");
                    String result = obj.cancelMovieTicketsUDP(customerID, movieID, movieName, numberOfTickets);

                    sendingResult = result + ";";
                }
                else if(method.equalsIgnoreCase("exchangeTickets"))
                {
                    String result = obj.exchangeTickets(customerID,movieID,movieName,oldMovieID,numberOfTickets);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieName: " + movieName + " ", sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
