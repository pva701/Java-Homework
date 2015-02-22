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
        Point[] arr = new Point[2];
        arr[0] = new Point(1, 2);
        arr[1] = new Point(2, 3);

        Point[] b = Arrays.copyOf(arr, arr.length);
        Arrays.sort(b, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                return o2.x - o1.x;
            }
        });

        ArrayList<Integer> ar = new ArrayList<Integer>();
        ar.add(1);
        ar.add(3);
        ar.add(3);
        ar.add(2);
        ar.add(5);
        ArraySet<Integer> set = new ArraySet<Integer>(ar);
        for (Integer e : set)
            System.out.println("el = " + e);
        System.out.println("lower = " + set.lower(1));
        System.out.println("lower = " + set.lower(2));
        System.out.println("lower = " + set.lower(10));
        System.out.println("lower = " + set.lower(5));

        System.out.println("floor = " + set.lower(1));
        System.out.println("floor = " + set.lower(2));
        System.out.println("floor = " + set.lower(10));
        System.out.println("floor = " + set.lower(5));
    }
}
