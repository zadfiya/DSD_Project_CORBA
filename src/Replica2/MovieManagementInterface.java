package Replica2;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MovieManagementInterface extends Remote {

    String addMovieSlot(String movieID, String movieName, int bookingCapacity) throws RemoteException;

    String removeMovieSlots(String movieID, String movieName) throws RemoteException;

    String listMovieShowAvailability(String movieName) throws RemoteException;

    /**
     * Both manager and Customer
     */
    String bookMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;

    String getBookingSchedule(String customerID) throws RemoteException;

    String cancelMovieTickets(String customerID, String movieID, String movieName, int numberOfTickets) throws RemoteException;

}
