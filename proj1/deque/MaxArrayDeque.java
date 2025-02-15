package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T> {
    private Comparator<T> comparator;

    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    public T max() {
//        if (this.size()==0) {
//            return null;
//        }
//        T max = this.get(0);
//        for (T i : this) {
//            if (comparator.compare(i, max) < 0) {
//                max = i;
//            }
//        }
//        return max;
        return max(comparator);
    }

    public T max(Comparator<T> c) {
        if (this.size() == 0) {
            return null;
        }
        T max = this.get(0);
        for (T i : this) {
            if (c.compare(i, max) > 0) {
                max = i;
            }
        }
        return max;
    }

}
