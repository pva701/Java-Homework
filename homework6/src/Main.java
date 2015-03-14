import java.util.ArrayList;
import java.util.Comparator;

/**
 * Created by pva701 on 3/15/15.
 */
public class Main {
    public static void main(String[] args) {
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(300);
        list.add(500);
        list.add(30);
        System.out.println("max = " + IterativeParallelism.maximum(2, list, Comparator.<Integer>naturalOrder()));
        System.out.println("min = " + IterativeParallelism.minimum(2, list, Comparator.<Integer>naturalOrder()));
        System.out.println("concat = " + IterativeParallelism.concat(2, list));
        System.out.println("map = " + IterativeParallelism.map(2, list, x->x+1));
        System.out.println("all = " + IterativeParallelism.all(2, list, x->x > 1));
        System.out.println("filter = " + IterativeParallelism.filter(2, list, x->x%2 == 0));
    }
}
