import java.io.IOException;
import java.net.*;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPClient {
    public static void main(String[] args) {
        byte[] buf = new byte[6];
        buf[0] = 'h';
        buf[1] = 'e';
        buf[2] = 'l';
        buf[3] = 'l';
        buf[4] = 'o';
        buf[5] = 0;
        try {
            //new DatagramSocket()
            //new DatagramSocket().send(
                    //new DatagramPacket(buf, 0, buf.length, InetAddress.getByName("127.0.0.1"), 3030));
        } catch (IOException e) {
            System.out.println("unknown host");
        }
    }
}
