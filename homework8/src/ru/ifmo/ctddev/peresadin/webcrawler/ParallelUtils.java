package ru.ifmo.ctddev.peresadin.webcrawler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by pva701 on 4/3/15.
 */
public class ParallelUtils {
    private ParallelUtils() {}

    public static abstract class AbstractThreadPool<T> {
        protected final BlockingQueue<T> tasksQueue = new LinkedBlockingQueue<>();
        protected Thread[] workers;

        public AbstractThreadPool(int threads) {
            threads = Math.min(threads, 300);
            workers = new Thread[threads];
            for (int i = 0; i < threads; ++i) {
                workers[i] = createThread();
                workers[i].start();
            }
        }

        public void execute(T task) {
            try {
                tasksQueue.put(task);
            } catch (InterruptedException ignore) {
                Thread.currentThread().interrupt();
            }
        }

        protected Thread createThread() {
            return new Thread(()->{
                while (!Thread.interrupted()) {
                    try {
                        handleTask(tasksQueue.take());
                    } catch (InterruptedException ignore) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
        }

        public void close() {
            for (Thread e : workers)
                e.interrupt();
        }

        protected abstract void handleTask(T task);
    }

    public static class ThreadPool extends AbstractThreadPool<Runnable> {
        public ThreadPool(int threads) {
            super(threads);
        }

        @Override
        public void handleTask(Runnable task) {
            task.run();
        }
    }

    public static class UrlDownloadTask {
        public Runnable runnable;
        public String host;
        public UrlDownloadTask(Runnable runnable, String host) {
            this.runnable = runnable;
            this.host = host;
        }
    }

    public static class HostLimitThreadPool extends AbstractThreadPool<UrlDownloadTask> {
        private final ConcurrentMap<String, ConcurrentLinkedQueue<UrlDownloadTask> > otherQue = new ConcurrentHashMap<>();
        private final ConcurrentMap<String, Integer> countDownloadsFromHost = new ConcurrentHashMap<>();

        private int perHost;
        public HostLimitThreadPool(int threads, int perHost) {
            super(threads);
            this.perHost = perHost;
        }

        @Override
        public void execute(UrlDownloadTask task) {
            String host = task.host;
            synchronized (this) {
                int cnt = countDownloadsFromHost.getOrDefault(host, 0);
                if (cnt < perHost) {
                    tasksQueue.add(task);
                    countDownloadsFromHost.put(host, cnt + 1);
                } else {
                    otherQue.putIfAbsent(host, new ConcurrentLinkedQueue<>());
                    otherQue.get(host).add(task);
                }
            }
        }

        @Override
        protected void handleTask(UrlDownloadTask task) {
            String host = task.host;
            task.runnable.run();
            synchronized (this) {
                countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) - 1);
                otherQue.putIfAbsent(host, new ConcurrentLinkedQueue<>());
                ConcurrentLinkedQueue<UrlDownloadTask> que = otherQue.get(host);
                while (countDownloadsFromHost.getOrDefault(host, 0) < perHost && que.size() != 0 && !Thread.interrupted()) {
                    countDownloadsFromHost.put(host, countDownloadsFromHost.getOrDefault(host, 0) + 1);
                    try {
                        tasksQueue.put(que.poll());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
