import java.io.IOException;
import java.net.*;
import java.util.Arrays;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPClient {
    public static int BUFFER_SIZE = 64*1024;
    public static void main(String[] args) throws UnknownHostException {
        byte[] b = new byte[]{97};
        System.out.println(new String(b));

        String ip = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int queries = Integer.parseInt(args[4]);
        InetAddress inetAddress = InetAddress.getByName(ip);

        for (int i = 1; i <= threads; ++i) {
            final int numThread = i;
            new Thread(()->{
                for (int j = 1; j <= queries; ++j) {
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket inPacket = new DatagramPacket(buf, 0, buf.length);
                    byte[] tmp = (prefix + numThread + "_" + j).getBytes();
                    for (int p = 0; p < tmp.length; ++p)
                        buf[p] = tmp[p];
                    try {
                        DatagramSocket socket = new DatagramSocket();
                        DatagramPacket outPacket = new DatagramPacket(buf, 0, tmp.length, inetAddress, port);
                        try {
                            socket.send(outPacket);
                        } catch (IOException e) {
                            System.out.println("fail send message!");
                            continue;
                        }

                        try {
                            socket.receive(inPacket);
                            System.out.println(new String(inPacket.getData(), 0, inPacket.getLength()));
                        } catch (IOException e) {
                            System.out.println("can't receive message");
                        }
                    } catch (SocketException e2) {
                        System.out.println("can't create socket!");
                    }
                }
            }).start();
        }
    }
}
