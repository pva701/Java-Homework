import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPServer {
    private DatagramSocket server;
    private ThreadPoolExecutor threadPoolExecutor;
    public static int BUFFER_SIZE = 64 * 1024;

    public HelloUDPServer(int port, int threads) throws IOException {
        server = new DatagramSocket(port);
        //threadPoolExecutor = new ThreadPoolExecutor(threads, threads, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new LinkedBlockingDeque<>());

        for (int i = 0; i < threads; ++i)
            new Thread(() -> {
                byte[] buf = new byte[BUFFER_SIZE];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                while (true) {
                    try {
                        server.receive(packet);
                        sendResponse(packet);
                    } catch (IOException e) {
                        System.out.println("ex receive");
                    }
                }
            }).start();
    }

    private void sendResponse(DatagramPacket packet) {
        String msg = "Hello, " + new String(packet.getData());
        byte[] sendBytes = msg.getBytes();
        try {
            server.send(new DatagramPacket(sendBytes, 0, sendBytes.length, packet.getAddress(), packet.getPort()));
        } catch (IOException e) {
            System.out.println("Server can't send response to client!");
        }
    }

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        new HelloUDPServer(port, threads);
    }
}
