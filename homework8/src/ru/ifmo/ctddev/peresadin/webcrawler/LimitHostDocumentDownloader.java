package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;
import info.kgeorgiy.java.advanced.crawler.URLUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class LimitHostDocumentDownloader {
    private final Map<String, Deque<String>> queueUrlForHost = new HashMap<>();
    private final Map<String, Deque<AfterDownload>> queueCallbacksForHost = new HashMap<>();
    private final Map<String, Integer> countDownloadsFromHost = new HashMap<>();
    private final Map<String, Document> loadedPages = new HashMap<>();
    private ParallelUtils.ThreadPool threadPool;
    private Downloader downloader;
    private int perHost;
    private int numDownloads;

    public static interface AfterDownload {
        void afterDownload(Document document);
    }

    public LimitHostDocumentDownloader(ParallelUtils.ThreadPool threadPool, Downloader downloader, int perHost) {
        this.threadPool = threadPool;
        this.downloader = downloader;
        this.perHost = perHost;
    }

    public synchronized boolean isEmpty() {
        return numDownloads == 0;
    }

    public static String hostOrNull(String url) {
        try {
            return URLUtils.getHost(url);
        } catch (IOException e) {
            return null;
        }
    }

    public synchronized  void download(final String url, final AfterDownload afterDownload) {
        final String host = hostOrNull(url);
        if (host == null) {
            return;
        }
        if (!countDownloadsFromHost.containsKey(host))
            countDownloadsFromHost.put(host, 0);
        if (countDownloadsFromHost.get(host) < perHost) {
            countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) + 1);
        } else {
            if (!queueUrlForHost.containsKey(host))
                queueUrlForHost.put(host, new LinkedList<>());
            queueUrlForHost.get(host).add(url);
            queueCallbacksForHost.get(host).add(afterDownload);
            return;
        }
        ++numDownloads;
        threadPool.execute(()->{
            try {
                final Document doc = tryDownload(url);
                synchronized (LimitHostDocumentDownloader.this) {
                    loadedPages.put(url, doc);
                    --numDownloads;
                    countDownloadsFromHost.put(host, countDownloadsFromHost.get(host) - 1);
                    if (queueUrlForHost.containsKey(host) && queueUrlForHost.get(host).size() != 0) {
                        String qUrl = queueUrlForHost.get(host).pollFirst();
                        queueUrlForHost.get(host).removeFirst();
                        download(qUrl, queueCallbacksForHost.get(host).pollFirst());
                    }
                    afterDownload.afterDownload(doc);
                    if (numDownloads == 0)
                        notify();
                }
            } catch (IOException e) {}
        });
    }

    private Document tryDownload(String url) throws IOException {
        synchronized (this) {
            if (loadedPages.containsKey(url))
                return loadedPages.get(url);
        }
        return downloader.download(url);
    }
}
