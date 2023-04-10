package Frontend;


public class MyRequest {
    private String function = "null";
    private String clientID = "null";
    private String movieName = "null";
    private String OldMovieName = "null";
    private String movieID = "null";
    private String OldMovieID = "null";
    private String FeIpAddress = FE.FE_IP_Address;
    private int bookingCapacity = 0;
    private int numberOfTickets = 0;
    private int sequenceNumber = 0;
    private String MessageType = "00";
    private int retryCount = 1;

    public MyRequest(String function, String clientID) {
        setFunction(function);
        setClientID(clientID);
    }

    public MyRequest(int rmNumber, String bugType) {
        setMessageType(bugType + rmNumber);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String getClientID() {
        return clientID;
    }

    public void setClientID(String clientID) {
        this.clientID = clientID;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public String getOldMovieName() {
        return OldMovieName;
    }

    public void setOldMovieName(String OldMovieName) {
        this.OldMovieName = OldMovieName;
    }

    public String getMovieID() {
        return movieID;
    }

    public void setMovieID(String movieID) {
        this.movieID = movieID;
    }

    public String getOldMovieID() {
        return OldMovieID;
    }

    public void setOldMovieID(String OldMovieID) {
        this.OldMovieID = OldMovieID;
    }

    public int getBookingCapacity() {
        return bookingCapacity;
    }

    public void setBookingCapacity(int bookingCapacity) {
        this.bookingCapacity = bookingCapacity;
    }

    public int getNumberOfTickets()
    {
        return numberOfTickets;
    }

    public void setNumberOfTickets(int numberOfTickets){ this.numberOfTickets = numberOfTickets;}

    public String noRequestSendError() {
        return "request: " + getFunction() + " from " + getClientID() + " not sent";
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public String getFeIpAddress() {
        return FeIpAddress;
    }

    public void setFeIpAddress(String feIpAddress) {
        FeIpAddress = feIpAddress;
    }

    public String getMessageType() {
        return MessageType;
    }

    public void setMessageType(String messageType) {
        MessageType = messageType;
    }

    public boolean haveRetries() {
        return retryCount > 0;
    }

    public void countRetry() {
        retryCount--;
    }

    //Message Format: Sequence_id;FrontIpAddress;Message_Type;function(addEvent,...);userID; newMovieID;newMovieName; oldMovieID; oldMovieName;bookingCapacity
    @Override
    public String toString() {
        return getSequenceNumber() + ";" +
                getFeIpAddress().toUpperCase() + ";" +
                getMessageType().toUpperCase() + ";" +
                getFunction().toUpperCase() + ";" +
                getClientID().toUpperCase() + ";" +
                getMovieID().toUpperCase() + ";" +
                getMovieName() + ";" +
                getOldMovieID().toUpperCase() + ";" +
                getNumberOfTickets()+";"+
                //getOldMovieName().toUpperCase() + ";" +
                getBookingCapacity()+";"+
                getNumberOfTickets();

    }
}
