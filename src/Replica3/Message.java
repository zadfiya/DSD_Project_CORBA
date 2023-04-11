package Replica3;

public class Message {

    public String FrontIpAddress,Function , MessageType, userID, newMovieID, newMovieName, oldMovieID;
    public int bookingCapacity, sequenceId,numberOfTickets;

    public Message(int sequenceId, String FrontIpAddress,String MessageType, String Function, String userID, String newMovieID,
                   String newMovieName,String oldMovieID,int numberOfTickets ,int bookingCapacity)
    {
        this.sequenceId = sequenceId;
        this.FrontIpAddress = FrontIpAddress;
        this.MessageType = MessageType;
        this.Function = Function;
        this.userID = userID;
        this.newMovieID = newMovieID;
        this.newMovieName = newMovieName;
        this.oldMovieID = oldMovieID;
        this.numberOfTickets = numberOfTickets;
        this.bookingCapacity = bookingCapacity;
    }
    @Override
    public String toString() {
        return sequenceId + ";" + FrontIpAddress + ";" +MessageType + ";" +Function + ";" +userID + ";" +newMovieID +
                ";" +newMovieName + ";" +oldMovieID + ";" +numberOfTickets + ";" +bookingCapacity;
    }

}
