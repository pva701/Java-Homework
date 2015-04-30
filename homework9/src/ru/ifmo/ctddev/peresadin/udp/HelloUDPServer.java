package ru.ifmo.ctddev.peresadin.udp;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.*;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPServer implements HelloServer {
    public static int BUFFER_SIZE = 64 * 1024;

    private ThreadPoolExecutor executor;
    private DatagramSocket server;
    private Thread loopThread;

    public static void barrierAwait(CyclicBarrier barrier) {
        try {
            barrier.await();
        } catch (BrokenBarrierException | InterruptedException e) {}
    }

    @Override
    public void start(int port, int threads) {
        try {
            server = new DatagramSocket(port);
        } catch (SocketException e) {
            //e.printStackTrace();
            return;
        }

        loopThread = new Thread(()->{
            executor = new ThreadPoolExecutor(threads, threads, Long.MAX_VALUE, TimeUnit.NANOSECONDS, new LinkedBlockingQueue<>());
            byte[] buf = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            while (!Thread.interrupted()) {
                try {
                    server.receive(packet);
                    String msg = "Hello, " + new String(packet.getData(), 0, packet.getLength());
                    //System.out.println("rec = " + msg);
                    InetAddress ip = packet.getAddress();
                    int portPacket = packet.getPort();
                    executor.submit(() -> sendResponse(msg, ip, portPacket));
                } catch (IOException e) {
                    //System.out.println("ex receive");
                    //e.printStackTrace();
                }
            }
        });
        loopThread.start();
    }

    @Override
    public void close() {
        server.close();
        executor.shutdown();
        loopThread.interrupt();
    }

    public HelloUDPServer() {
    }

    private void sendResponse(String msg, InetAddress ip, int port) {
        byte[] sendBytes = msg.getBytes();
        try {
            server.send(new DatagramPacket(sendBytes, 0, sendBytes.length, ip, port));
        } catch (IOException e) {
            //System.out.println("Server can't send response to client!");
            //e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("IN MAIN");
        int port = Integer.parseInt(args[0]);
        int threads = Integer.parseInt(args[1]);
        new HelloUDPServer().start(port, threads);
    }

    /*for (int i = 0; i < threads; ++i)
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
        }).start();*/
}
