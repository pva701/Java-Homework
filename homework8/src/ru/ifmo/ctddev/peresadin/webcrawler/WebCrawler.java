package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private ParallelUtils.HostLimitThreadPool downloadThreadPool;
    private ParallelUtils.ThreadPool extractorThreadPool;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        downloadThreadPool = new ParallelUtils.HostLimitThreadPool(downloaders, perHost);
        extractorThreadPool = new ParallelUtils.ThreadPool(extractors);
    }

    private void decCounter(AtomicInteger counter) {
        synchronized (counter) {
            counter.decrementAndGet();
            if (counter.get() == 0)
                counter.notify();
        }
    }

    private void runDownload(
            String url,
            final int curDepth,
            final int maxDepth,
            final List<String> result,
            final ConcurrentMap<String, Future<Document> > loadedPages,
            final ConcurrentMap<Document, Future<List<String>>> extractedPages,
            final AtomicInteger counterDownloads) {
        counterDownloads.incrementAndGet();

        String host = null;
        try {
            host = URLUtils.getHost(url);
        } catch (IOException e) {
            printEx(e);
        }
        if (host == null) {
            decCounter(counterDownloads);
            return;
        }

        downloadThreadPool.execute(new ParallelUtils.UrlDownloadTask(
        ()->{
            FutureTask<Document> futureDownload = new FutureTask<>(()->{
                try {
                    return downloader.download(url);
                } catch (IOException e) {
                    printEx(e);
                    return null;
                }
            });

            if (loadedPages.putIfAbsent(url, futureDownload) == null) {
                futureDownload.run();
            }

            try {
                final Document doc = loadedPages.get(url).get();
                if (doc == null) {
                    decCounter(counterDownloads);
                    return;
                }

                extractorThreadPool.execute(() -> {
                    FutureTask<List<String>> futureExtract = new FutureTask<>(()->{
                        try {
                            return doc.extractLinks();
                        } catch (IOException e) {
                            printEx(e);
                            return null;
                        }
                    });
                    if (extractedPages.putIfAbsent(doc, futureExtract) == null) {
                        futureExtract.run();
                    }

                    try {
                        final List<String> links = extractedPages.get(doc).get();
                        synchronized (result) {
                            result.addAll(links);
                        }
                        if (curDepth < maxDepth) {
                            for (String e : links)
                                runDownload(e, curDepth + 1, maxDepth, result, loadedPages, extractedPages, counterDownloads);
                        }
                    } catch (Exception e) {
                        printEx(e);
                    } finally {
                        decCounter(counterDownloads);
                    }
                });
            } catch (Exception e) {
                printEx(e);
                decCounter(counterDownloads);
            }
        }, host));

    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        List<String> ret = new ArrayList<>();
        ret.add(url);
        AtomicInteger counter = new AtomicInteger(0);
        runDownload(url, 1, depth, ret, new ConcurrentHashMap<>(), new ConcurrentHashMap<>(), counter);
        synchronized (counter) {
            while (counter.get() != 0) {
                try {
                    counter.wait();
                } catch (InterruptedException e) {}
            }
        }
        //System.out.println("links size = " + ret.size());
        return ret;
    }

    @Override
    public void close() {
        downloadThreadPool.close();
        extractorThreadPool.close();
    }

    public static boolean DEBUG = false;
    public static void printEx(Exception e) {
        if (DEBUG) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        String url = args[0];
        int download = 5;
        int extractor = 5;
        int perHost = 5;
        if (args.length > 1)
            download = Integer.parseInt(args[1]);
        if (args.length > 2)
            extractor = Integer.parseInt(args[2]);
        if (args.length > 3)
            perHost = Integer.parseInt(args[3]);
        Crawler crawler = new WebCrawler(new CachingDownloader(), download, extractor, perHost);
        List<String> list = crawler.download(url, 2);
        System.out.println("links");
        for (String s : list)
            System.out.println("link = " + s);
        crawler.close();
    }
}
