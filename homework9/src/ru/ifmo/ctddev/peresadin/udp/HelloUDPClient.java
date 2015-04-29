package ru.ifmo.ctddev.peresadin.udp;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by pva701 on 4/27/15.
 */
public class HelloUDPClient implements HelloClient {
    public static int BUFFER_SIZE = 64*1024;

    private void threadDone(ReentrantLock lock, Condition allDoneCond, AtomicInteger threadCounter) {
        lock.lock();
        if (threadCounter.decrementAndGet() == 0)
            allDoneCond.signal();
        lock.unlock();
    }

    @Override
    public void start(String host, int port, String prefix, int requests, int threads)  {
        try {
            AtomicInteger countThreads = new AtomicInteger(threads);
            ReentrantLock lock = new ReentrantLock();
            Condition allDoneCond = lock.newCondition();

            InetAddress inetAddress = InetAddress.getByName(host);
            for (int i = 0; i < threads; ++i) {
                final int numThread = i;
                new Thread(()->{
                    //System.out.println("thread = " + numThread + " req = " + requests);
                    byte[] buf = new byte[BUFFER_SIZE];
                    DatagramPacket inPacket = new DatagramPacket(buf, 0, buf.length);
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(500);
                        for (int j = 0; j < requests; ++j) {
                            byte[] tmp = (prefix + numThread + "_" + j).getBytes();
                            //System.arraycopy(tmp, 0, buf, 0, tmp.length);
                            DatagramPacket outPacket = new DatagramPacket(tmp, 0, tmp.length, inetAddress, port);
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
                                --j;
                                System.out.println("can't receive message");
                            }
                        }
                    } catch (SocketException e) {}
                    threadDone(lock, allDoneCond, countThreads);
                }).start();
            }

            try {
                lock.lock();
                while (countThreads.get() != 0)
                    allDoneCond.await();
                lock.unlock();
            } catch (InterruptedException e) {}
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