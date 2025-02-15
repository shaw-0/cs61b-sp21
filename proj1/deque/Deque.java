package deque;

import java.util.Iterator;

public interface Deque<T> {
    /**
     * Add method:
     * must not involve any looping or recursion.
     * A single such operation must take “constant time”.
     * i.e. execution time should not depend on the size of the deque.
     */
    public void addFirst(T item);

    public void addLast(T item);

    default boolean isEmpty() {
        if (this.size() == 0) {
            return true;
        }
        return false;
    }

    public int size();

    public void printDeque();

    public T removeFirst();

    public T removeLast();

    public T get(int index);

}