package Replica4;

public class Message {
    public String FrontIpAddress,Function , MessageType, userID, newMovieID, newMovieType, oldMovieID;
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
        this.newMovieType = newMovieType;
        this.oldMovieID = oldMovieID;
        this.bookingCapacity = bookingCapacity;
    }
    @Override
    public String toString() {
        return sequenceId + ";" + FrontIpAddress + ";" +MessageType + ";" +Function + ";" +userID + ";" +newMovieID +
                ";" +newMovieType + ";" +oldMovieID +  ";" + numberOfTickets + ";" +bookingCapacity;
    }
}
