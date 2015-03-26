package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Created by pva701 on 3/15/15.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException{
        /*System.out.println(Runtime.getRuntime().availableProcessors());
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(300);
        list.add(500);
        list.add(30);
        System.out.println("max = " + new IterativeParallelism().maximum(2, list, Comparator.<Integer>naturalOrder()));
        System.out.println("min = " + new IterativeParallelism().minimum(2, list, Comparator.<Integer>naturalOrder()));
        System.out.println("concat = " + new IterativeParallelism().concat(2, list));
        System.out.println("map = " + new IterativeParallelism().map(2, list, x->x+1));
        System.out.println("all = " + new IterativeParallelism().all(2, list, x->x > 1));
        System.out.println("filter = " + new IterativeParallelism().filter(2, list, x->x%2 == 0));*/
        //System.out.println("name = " + ParallelUtils.ParallelMapperImpl.class.getName());
        final int T = 10000;
        List<Integer> a = new ArrayList<>(T);
        Random rnd = new Random();
        for (int i = 0; i < T; ++i)
            a.add(rnd.nextInt());

        ParallelMapperImpl mapper = new ParallelMapperImpl(2);
        System.out.println("start");
        System.out.println("result = " + new IterativeParallelism(mapper).any(2, a, x->x == 0));
        mapper.close();
        /*List <Integer> r = new ArrayList<>();
        long l = System.currentTimeMillis();
        for (int i = 0; i < a.size(); ++i)
            r.add(a.get(i) % 43);
        System.out.println("time stupid = " + (System.currentTimeMillis() - l) / 1000.0);*/

        /*long l = System.currentTimeMillis();
        System.out.println("start");

        List<String> res = mapper.map(x -> {
            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
            }
            return Integer.toBinaryString(x);
        }, a);
        for (int i = 0; i < 10; ++i)
            System.out.print(res.get(i) + " ");
        System.out.println("");
        System.out.println("stop");
        System.out.println("time threads = " + (System.currentTimeMillis() - l) / 1000.0);
        mapper.close();*/
    }
}
