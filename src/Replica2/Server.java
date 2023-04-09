package Replica2;

/**
 * author: Naren Zadafiya (40232646)
 */
public class Server {
    public Server() {}
    public static void main(String args[])
    {
        try
        {

//            ServerInstance atwaterServer = new ServerInstance("ATW");
//            ServerInstance verdunServer = new ServerInstance("VER");
//            ServerInstance outremontServer = new ServerInstance("OUT");
            Runnable task1 = () -> {
                try {
                    ServerInstance atwaterServer = new ServerInstance("ATW", args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Runnable task2 = () -> {
                try {
                    ServerInstance verdunServer = new ServerInstance("VER", args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Runnable task3 = () -> {
                try {
                    ServerInstance outremontServer = new ServerInstance("OUT", args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
            Thread thread1 = new Thread(task1);
            thread1.start();
            Thread thread2 = new Thread(task2);
            thread2.start();
            Thread thread3 = new Thread(task3);
            thread3.start();
        }


        catch(Exception e)
        {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
