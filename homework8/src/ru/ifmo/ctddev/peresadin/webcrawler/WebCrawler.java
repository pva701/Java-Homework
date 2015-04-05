package ru.ifmo.ctddev.peresadin.webcrawler;

import info.kgeorgiy.java.advanced.crawler.Crawler;
import info.kgeorgiy.java.advanced.crawler.Document;
import info.kgeorgiy.java.advanced.crawler.Downloader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebCrawler implements Crawler {
    private Downloader downloader;
    private int downloaders;
    private int extractors;
    private int perHost;
    private ParallelUtils.ThreadPool downloadThreadPool;
    private ParallelUtils.ThreadPool extractorThreadPool;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = downloaders;
        this.extractors = extractors;
        this.perHost = perHost;
        downloadThreadPool = new ParallelUtils.ThreadPool(downloaders);
        extractorThreadPool = new ParallelUtils.ThreadPool(extractors);
    }

    private void runDownload(
            String url,
            final int curDepth,
            final int maxDepth,
            List<String> result) {

        downloadThreadPool.execute(()->{
            try {
                extractorThreadPool.execute(()->{
                    try {
                        List<String> links = doc.extractLinks();
                        synchronized (result) {
                            result.addAll(links);
                        }
                        if (curDepth < maxDepth) {
                            for (String e : links)
                                runDownload(e, curDepth + 1, maxDepth, result, loadedPage);
                        }
                    } catch (IOException e) {
                        //ignore
                    }
                });
            } catch (IOException e) {
                //ignore
            }
        });
    }

    @Override
    public List<String> download(String url, int depth) throws IOException {
        List<String> ret = new ArrayList<>();
        runDownload(url, 1, depth, ret, new LimitHostDownloader(downloadThreadPool, downloader, perHost));
    }

    @Override
    public void close() {

    }
}
