import java.util.*;
/**
 * Created by pva701 on 2/20/15.
 */
public class Main {

    static class Point {
        int x, y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(1);
        ar.add(3);
        ar.add(3);
        ar.add(2);
        ar.add(5);
        NavigableSet<Integer> set = new ArraySet<Integer>(ar);
        for (Integer e : set)
            System.out.println("el = " + e);
        System.out.println("lower = " + set.lower(1));
        System.out.println("lower = " + set.lower(2));
        System.out.println("lower = " + set.lower(10));
        System.out.println("lower = " + set.lower(5));

        System.out.println("floor = " + set.floor(1));
        System.out.println("floor = " + set.floor(2));
        System.out.println("floor = " + set.floor(10));
        System.out.println("floor = " + set.floor(5));

        System.out.println("higher = " + set.higher(1));
        System.out.println("higher = " + set.higher(2));
        System.out.println("higher = " + set.higher(10));
        System.out.println("higher = " + set.higher(5));

        System.out.println("ceiling = " + set.ceiling(1));
        System.out.println("ceiling = " + set.ceiling(2));
        System.out.println("ceiling = " + set.ceiling(10));
        System.out.println("ceiling = " + set.ceiling(5));

        System.out.println("======reverse set=======");
        set = set.descendingSet();
        System.out.println("lower = " + set.lower(-1));
        System.out.println("lower = " + set.lower(1));
        System.out.println("lower = " + set.lower(2));
        System.out.println("lower = " + set.lower(10));
        System.out.println("lower = " + set.lower(5));

        System.out.println("floor = " + set.floor(1));
        System.out.println("floor = " + set.floor(2));
        System.out.println("floor = " + set.floor(10));
        System.out.println("floor = " + set.floor(5));

        System.out.println("higher = " + set.higher(1));
        System.out.println("higher = " + set.higher(2));
        System.out.println("higher = " + set.higher(10));
        System.out.println("higher = " + set.higher(5));

        System.out.println("ceiling = " + set.ceiling(1));
        System.out.println("ceiling = " + set.ceiling(2));
        System.out.println("ceiling = " + set.ceiling(10));
        System.out.println("ceiling = " + set.ceiling(5));
    }
}
