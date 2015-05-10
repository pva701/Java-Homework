package ru.ifmo.ctddev.peresadin.udp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPClient implements HelloClient {
    //public static int BUFFER_SIZE = 1500;
    public static final Charset DEF_CHARSET = Charset.forName("UTF-8");

    private static void barrierAwait(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {}
    }

    @Override
    public void start(String host, int port, String prefix, int requests, int threads)  {
        try {
            InetAddress inetAddress = InetAddress.getByName(host);
            CyclicBarrier barrier = new CyclicBarrier(threads + 1);
            for (int i = 0; i < threads; ++i) {
                final int numThread = i;
                new Thread(()->{
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(500);
                        byte[] inBuf = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket inPacket = new DatagramPacket(inBuf, 0, inBuf.length);
                        for (int j = 0; j < requests; ++j) {
                            String reqStr = (prefix + numThread + "_" + j);
                            byte[] tmp = reqStr.getBytes(DEF_CHARSET);
                            DatagramPacket outPacket = new DatagramPacket(tmp, 0, tmp.length, inetAddress, port);
                            try {
                                socket.send(outPacket);
                            } catch (IOException e) {
                                System.out.println("fail send message!");
                                 break;
                            }

                            try {
                                socket.receive(inPacket);
                                String resStr = new String(inPacket.getData(), 0, inPacket.getLength());
                                if (!resStr.equals("Hello, " + reqStr)) {
                                    --j;
                                }
                                System.out.println(new String(inPacket.getData(), 0, inPacket.getLength()));
                            } catch (IOException e) {
                                --j;
                                //System.out.println("can't receive message");
                            }
                        }
                    } catch (SocketException e) {}
                    barrierAwait(barrier);
                }).start();
            }
            barrierAwait(barrier);
        } catch (UnknownHostException e) {
        }
    }

    public static void main(String[] args) throws UnknownHostException {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String prefix = args[2];
        int threads = Integer.parseInt(args[3]);
        int queries = Integer.parseInt(args[4]);
        new HelloUDPClient().start(host, port, prefix, threads, queries);
    }
}
