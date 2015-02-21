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

        System.out.println(arr[0].x);
        new TreeSet<>()
    }
}
