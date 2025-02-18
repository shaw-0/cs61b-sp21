package flik;

import org.junit.Test;
import static org.junit.Assert.*;

public class TestFlik {
    @Test
    public void sameTest() {
        for (int i = 0; i < 1000; i++) {
            int j = i;
            assertTrue("error occurs at " + i, Flik.isSameNumber(i, j));
        }
    }
}
