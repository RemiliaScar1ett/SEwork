// client/src/Main.java
import net.TcpClient;

public class Main {
    public static void main(String[] args) {
        TcpClient client=new TcpClient("127.0.0.1",5000);
        try {
            client.connect();
            System.out.println("Connected to server.");

            String req="{\"requestId\":\"1\",\"action\":\"Ping\","
                    +"\"data\":{\"msg\":\"Hello from Java\"}}";
            System.out.println("[Client] send: "+req);

            String resp=client.sendAndReceive(req);
            System.out.println("[Client] recv: "+resp);

            client.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
