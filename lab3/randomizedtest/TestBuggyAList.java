package randomizedtest;

import edu.princeton.cs.algs4.In;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
  // YOUR TESTS HERE
    @Test
    public void threeAddThreeRemove(){
        BuggyAList<Integer> testList = new BuggyAList<>();
        testList.addLast(4);
        testList.addLast(5);
        testList.addLast(6);
        int result = testList.removeLast();
        assertEquals(6, result);
        result = testList.removeLast();
        assertEquals(5, result);
        result = testList.removeLast();
        assertEquals(4, result);
    }

    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> L_test = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                L_test.addLast(randVal);
//                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
//                System.out.println("size: " + size);
            } else if (operationNumber == 2) {
                if (L.size() > 0) {
//                    System.out.println("last: " + L.getLast());
                    assertEquals(L.getLast(), L_test.getLast());
                }
            } else if (operationNumber == 3) {
                if (L.size() > 0) {
                    int exp = L.removeLast();
                    int act = L_test.removeLast();
//                    System.out.println("removeLast(" + exp + ")");
                    assertEquals(exp, act);
                }
            }
        }
    }
}
