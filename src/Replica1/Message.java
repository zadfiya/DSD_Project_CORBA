package Replica1;

public class Message {
    public String FrontIpAddress,Function , MessageType, userID, newMovieID, newMovieName, oldMovieID;
    public int bookingCapacity, sequenceId , numberOfTickets;

    public Message(int sequenceId, String FrontIpAddress,String MessageType, String Function, String userID, String newMovieID,
                   String newMovieType,String oldMovieID,int numberOfTickets,int bookingCapacity)
    {
        this.sequenceId = sequenceId;
        this.FrontIpAddress = FrontIpAddress;
        this.MessageType = MessageType;
        this.Function = Function;
        this.userID = userID;
        this.newMovieID = newMovieID;
        this.newMovieName = newMovieType;
        this.oldMovieID = oldMovieID;
        this.bookingCapacity = bookingCapacity;
    }
    @Override
    public String toString() {
        return sequenceId + ";" + FrontIpAddress + ";" +MessageType + ";" +Function + ";" +userID + ";" +newMovieID +
                ";" +newMovieName + ";" +oldMovieID +  ";" + numberOfTickets + ";" +bookingCapacity;
    }
}
