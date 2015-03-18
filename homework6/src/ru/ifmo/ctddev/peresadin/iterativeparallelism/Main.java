package ru.ifmo.ctddev.peresadin.iterativeparallelism;

import ru.ifmo.ctddev.peresadin.iterativeparallelism.IterativeParallelism;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by pva701 on 3/15/15.
 */
public class Main {
    public static void main(String[] args) throws InterruptedException{
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
        System.out.println("filter = " + new IterativeParallelism().filter(2, list, x->x%2 == 0));
    }
}
