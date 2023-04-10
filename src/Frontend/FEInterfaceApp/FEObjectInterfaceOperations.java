package Frontend.FEInterfaceApp;


/**
* FEInterfaceApp/FEObjectInterfaceOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from ./FEInterface.idl
* Sunday, April 9, 2023 7:31:49 o'clock PM EDT
*/

public interface FEObjectInterfaceOperations 
{

  /**
          * Only for admin
          */
  String addMovieSlot (String adminID, String movieID, String movieName, int bookingCapacity);
  String removeMovieSlots (String adminID, String movieID, String movieName);
  String listMovieShowAvailability (String adminID, String movieName);

  /**
               * Both admin and Customer
               */
  String bookMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
  String getBookingSchedule (String customerID);
  String cancelMovieTickets (String customerID, String movieID, String movieName, int numberOfTickets);
  String exchangeTickets (String customerID, String newMovieID, String newMovieName, String oldMovieID, int numberOfTickets);
  void shutdown ();
} // interface FEObjectInterfaceOperations
