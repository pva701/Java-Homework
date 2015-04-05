package ru.ifmo.ctddev.peresadin.webcrawler;

import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by pva701 on 4/3/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static class ThreadPool {
        private final Deque<Runnable> tasksQueue = new LinkedList<>();

        public ThreadPool(int threads) {
            for (int i = 0; i < threads; ++i) {
                new Thread(()->{
                    Runnable curExec;
                    synchronized (tasksQueue) {
                        while (tasksQueue.isEmpty()) {
                            try {
                                tasksQueue.wait();
                            } catch (InterruptedException e) {
                                System.err.println("task queue ex!");
                            }
                        }
                        curExec = tasksQueue.pollFirst();
                    }
                    curExec.run();
                }).start();
            }
        }

        public void execute(Runnable runnable) {
            synchronized (tasksQueue) {
                tasksQueue.add(runnable);
                tasksQueue.notify();
            }
        }
    }
}
