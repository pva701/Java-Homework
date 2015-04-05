package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.function.Function;

/**
 * Created by pva701 on 3/26/15.
 */

public class ParallelMapperImpl implements ParallelMapper {
    public enum State {RUNNING, NOT_RUNNING, CLOSED}

    private int workedThreads;
    private ParallelUtils.ReusedWorker[] workers;
    private State state;
    private Deque<Thread> queue = new ArrayDeque<>();

    /**
     * Default constructor
     */
    public ParallelMapperImpl() {}

    /**
     * Constructor from threads.
     * @param threads number of worker threads
     */
    public ParallelMapperImpl(int threads) {
        state = State.NOT_RUNNING;
        workers = new ParallelUtils.ReusedWorker[threads];
        for (int i = 0; i < threads; ++i)
            workers[i] = new ParallelUtils.ReusedWorker();
    }
    /**
     * Does parallel evaluation {@param f} on {@param args}. Degree of parallelism definite by constructor.
     * @param f function for mapping.
     * @param args arguments for mapping.
     * @param <T> template param.
     * @param <R> template param.
     * @return result of evaluation.
     * @throws InterruptedException if ParallelMapper instance was close.
     */
    public <T, R> List<R> map(
            Function<? super T, ? extends R> f,
            List<? extends T> args)
            throws InterruptedException {
        synchronized (this) {
            queue.add(Thread.currentThread());
            while (state == State.RUNNING || queue.peekFirst() != Thread.currentThread()) {
                wait();
            }
            queue.removeFirst();
            if (state == State.CLOSED) {
                throw new InterruptedException();
            }
            state = State.RUNNING;
        }

        workedThreads = (workers.length < args.size() ? workers.length : args.size());
        int len = args.size() / workedThreads;
        int startPosition = 0;
        List<R> result = new ArrayList<>();
        for (int i = 0; i < args.size(); ++i) {
            result.add(null);
        }
        int totalThreads = workedThreads;

        for (int i = 0; i < totalThreads; ++i) {
            int curLength = len;
            if (i < args.size() % totalThreads) {
                ++curLength;
            }

            final int l = startPosition;
            final int r = startPosition + curLength - 1;
            startPosition += curLength;

            workers[i].run(() -> {
                for (int j = l; j <= r; ++j)
                    result.set(j, f.apply(args.get(j)));
            },
                    () -> {
                        synchronized (ParallelMapperImpl.this) {
                            --workedThreads;
                            if (workedThreads == 0) {
                                ParallelMapperImpl.this.notifyAll();
                            }
                        }
                    });
        }

        synchronized (this) {
            while (workedThreads != 0) {
                wait();
            }
            state = State.NOT_RUNNING;
            notifyAll();
        }
        return result;
    }

    /**
     * Close all worker threads.
     * @throws InterruptedException if already closed
     */
    public synchronized void close() throws InterruptedException {
        if (state == State.CLOSED)
            throw new InterruptedException();
        for (int i = 0; i < workers.length; ++i)
            workers[i].close();
        state = State.CLOSED;
    }

}

