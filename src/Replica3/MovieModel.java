package Replica3; 
/**
 * author: Nayankumar Sorathiya (40227432)
 */
import java.util.ArrayList;
import java.util.List;



public class MovieModel {


    private String movieName;
    private String movieID;
    private String movieTheater;
    private int bookingCapacity;
    private String movieDate;
    private String movieTimeSlot;
    private List<String> registeredClients;

    private int bookedSeats;

    public int getBookedSeats() {
        return bookedSeats;
    }

    /**
     *
     * @param n
     */
    public void seatBooked(int n)
    {
         this.bookedSeats=this.bookedSeats+n;
    }

    /**
     *
     * @param n
     */
    public void seatCanceled(int n)
    {
        this.bookedSeats = this.bookedSeats-n;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    /**
     *
     * @param movieName
     * @param movieID
     * @param bookingCapacity
     */
    public MovieModel(String movieName, String movieID, int bookingCapacity) {
        this.movieID = movieID;
        this.movieName = movieName;
        this.bookingCapacity = bookingCapacity;
        this.movieTimeSlot = Constant.identifyMovieTimeSlot(movieID);
        this.movieTheater = Constant.detectServer(movieID);
        this.movieDate = Constant.identifyMovieDate(movieID);
        this.bookedSeats=0;
        registeredClients = new ArrayList<>();
    }







    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getEventServer() {
        return movieTheater;
    }

    public void setMovieTheater(String movieTheater) {
        this.movieTheater = movieTheater;
    }

    public int getBookingCapacity() {
        return bookingCapacity;
    }

    public void setBookingCapacity(int bookingCapacity) {
        this.bookingCapacity = bookingCapacity;
    }

    public int getTheaterRemainCapacity() {
        return bookingCapacity - bookedSeats;
    }



    public String getMovieDate() {
        return movieDate;
    }

    public void setMovieDate(String movieDate) {
        this.movieDate = movieDate;
    }

    public String getMovieTimeSlot() {
        return movieTimeSlot;
    }

    public void setMovieTimeSlot(String movieTimeSlot) {
        this.movieTimeSlot = movieTimeSlot;
    }

    public boolean isFull() {
        return getBookingCapacity() == registeredClients.size();
    }

    public List<String> getRegisteredClientIDs() {
        return registeredClients;
    }

    public void setRegisteredClientsIDs(List<String> registeredClientsIDs) {
        this.registeredClients = registeredClientsIDs;
    }

    /**
     *
     * @param registeredClientID
     * @return
     */
    public int addRegisteredClientID(String registeredClientID) {
        if (!isFull()) {
            if (registeredClients.contains(registeredClientID)) {
                return Constant.ALREADY_BOOKED_FLAG;
            } else {
                registeredClients.add(registeredClientID);
                return Constant.ADD_SUCCESS_FLAG;
            }
        } else {
            return Constant.SHOW_FULL_FLAG;
        }
    }

    /**
     *
     * @param registeredClientID
     * @return
     */
    public boolean removeRegisteredClientID(String registeredClientID) {
        return registeredClients.remove(registeredClientID);
    }

    @Override
    /**
     *
     */
    public String toString() {
        return " (" + getMovieID() + ") in the " + getMovieTimeSlot() + " of " + getMovieDate() + " Total[Remaining] Capacity: " + getBookingCapacity() + "[" + getTheaterRemainCapacity() + "]";
    }

}
