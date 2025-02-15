package deque;

import jh61b.junit.In;
import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    public void intArrayDequeTest() {
        class IntComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        }
        MaxArrayDeque<Integer> intArrayDeque = new MaxArrayDeque<>(new IntComparator());
        for (int i = 0; i < 100; i++) {
            intArrayDeque.addLast(i);
        }
        int max = intArrayDeque.max();
        assertEquals(max, 99);
    }

    @Test
    public void stringArrayDequeTest() {
        class StringComparator implements Comparator<String> {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        }
        MaxArrayDeque<String> stringArrayDeque = new MaxArrayDeque<>(new StringComparator());
        stringArrayDeque.addLast("I");
        stringArrayDeque.addLast("ich");
        stringArrayDeque.addLast("am");
        stringArrayDeque.addLast("watashi");
        stringArrayDeque.addLast("wo");
        stringArrayDeque.addLast("bin");
        stringArrayDeque.addLast("ha");
        stringArrayDeque.addLast("shi");
        String max = stringArrayDeque.max();
        assertEquals(max, "wo");
    }

    @Test
    public void nullArrayDequeTest() {
        class IntComparator implements Comparator<Integer> {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        }
        MaxArrayDeque<Integer> intArrayDeque = new MaxArrayDeque<>(new IntComparator());
        assertEquals(intArrayDeque.max(), null);
    }

}
