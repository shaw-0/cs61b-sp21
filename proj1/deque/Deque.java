package deque;

import java.util.Iterator;

public interface Deque<T> {
    /**
     * Add method:
     * must not involve any looping or recursion.
     * A single such operation must take “constant time”.
     * i.e. execution time should not depend on the size of the deque.
     */
    void addFirst(T item);

    void addLast(T item);

    default boolean isEmpty() {
        if (this.size() == 0) {
            return true;
        }
        return false;
    }

    int size();

    void printDeque();

    T removeFirst();

    T removeLast();

    T get(int index);

}
