/**

* @author  Krutik Gevariya

*/

package Replica1.Server;


import Replica1.Constant;
import Replica1.DMTBSapp.DMTBS;
import Replica1.DMTBSapp.DMTBSHelper;
import Replica1.Logger.Logger;
import Replica1.ServerImplementation.ServerImpl;
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

public class ServerInstance {

    private String serverID;
    private String serverName;
    private int serverRegistryPort;
    private int serverUdpPort;

    public ServerInstance(String serverID , String[] args) throws Exception{
        this.serverID = serverID;
        switch(serverID){
            case "ATW":
                serverName = Replica2.Constant.SERVER_ATWATER;
                serverRegistryPort = Replica2.Constant.ATWATER_REGISTRY_PORT;
                serverUdpPort = Constant.ATWATER_SERVER_PORT;
                break;
            case "OUT":
                serverName = Constant.SERVER_VERDUN;
                serverRegistryPort = Constant.VERDUN_REGISTRY_PORT;
                serverUdpPort = Constant.VERDUN_SERVER_PORT;
                break;
            case "VER":
                serverName = Constant.SERVER_OUTREMONT;
                serverRegistryPort = Constant.OUTREMONT_REGISTRY_PORT;
                serverUdpPort = Constant.OUTREMONT_SERVER_PORT;
                break;
        }

        try {
            // create and initialize the ORB //// get reference to rootpoa &amp; activate
            // the POAManager
            ORB orb = ORB.init(args, null);
            // -ORBInitialPort 1050 -ORBInitialHost localhost
            POA rootpoa = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
            rootpoa.the_POAManager().activate();

            // create servant and register it with the ORB
            ServerImpl servant = new ServerImpl(serverID, serverName);
            servant.setORB(orb);

            // get object reference from the servant
            org.omg.CORBA.Object ref = rootpoa.servant_to_reference(servant);
            DMTBS href = DMTBSHelper.narrow(ref);

            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);

            NameComponent[] path = ncRef.to_name(serverID);
            ncRef.rebind(path, href);

            System.out.println(serverName + " Server is Up & Running");
            Logger.serverLog(serverID, " Server is Up & Running");

//            addTestData(servant);
            Runnable task = () -> {
                listenForRequest(servant, serverUdpPort, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();

            // wait for invocations from clients
            while (true) {
                orb.run();
            }
        } catch (Exception e) {
//            System.err.println("Exception: " + e);
            e.printStackTrace(System.out);
            Logger.serverLog(serverID, "Exception: " + e);
        }

        System.out.println(serverName + " Server Shutting down");
        Logger.serverLog(serverID, " Server Shutting down");
    }

    private void listenForRequest(ServerImpl remoteObject, int serverUdpPort, String serverName, String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult ="";
        try{
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer =new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Logger.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while(true){
                DatagramPacket request = new DatagramPacket(buffer,buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(),0,request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String customerID = parts[1];
                String movieName = parts[2];
                String movieID = parts[3];
                String OldMovieID = parts[4];
                int numberOfTickets = Integer.parseInt(parts[4]);
                if(method.equalsIgnoreCase("bookMovie")){
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " eventType: " + movieName + " ", " ...");
                    String result = remoteObject.bookMovieTickets(customerID,movieID,movieName,numberOfTickets);
                    sendingResult = result + ";";
                }else if(method.equalsIgnoreCase("listMovieAvailability")){
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieType: " + movieName + " ", " ...");
                    String result = remoteObject.listMovieShowsAvailabilityUDP(movieName);
                    sendingResult = result + ";";
                }else if(method.equalsIgnoreCase(("removeMovie"))){
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", " ...");
                    String result = remoteObject.removeMovieSlotsUDP(movieID,movieName,customerID,numberOfTickets);
                    sendingResult = result + ";";
                }else if(method.equalsIgnoreCase("cancelMovie")){
                    Logger.serverLog(serverID, customerID, " UDP request received " + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", " ...");
                    String result = remoteObject.cancelMovieTickets(customerID,movieID,movieName,numberOfTickets);
                    sendingResult = result + ";";
                }
                else if(method.equalsIgnoreCase("exchangeTickets"))
                {
                    String result = remoteObject.exchangeTickets(customerID,OldMovieID,movieID,movieName,numberOfTickets);
                    sendingResult = result + ";";
                }
                byte[] sendData =sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData,sendingResult.length(),request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Logger.serverLog(serverID, customerID, " UDP reply sent " + method + " ", " movieID: " + movieID + " movieType: " + movieName + " ", sendingResult);
            }

        }catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }finally {
            if(aSocket != null){
                aSocket.close();
            }
        }
    }

}
