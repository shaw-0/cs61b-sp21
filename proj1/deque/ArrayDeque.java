package deque;

import java.util.Iterator;

public class ArrayDeque<T> implements Deque<T> {
    private int size;
    private int capacity = 100;
    private T[] array;
    public ArrayDeque() {
        size = 0;
        array = (T[]) new Object[capacity];
    }

    /** Add method:
     * must not involve any looping or recursion.
     * A single such operation must take “constant time”.
     * i.e. execution time should not depend on the size of the deque.
     */
    @Override
    public void addFirst(T item) {
        /* Adds an item of type T to the front of the deque.
        You can assume that item is never null.
        */
        if (size == capacity) {
            capacity = 2 * capacity;
        }
        T[] tmp = (T[]) new Object[capacity];
        tmp[0] = item;
        System.arraycopy(array, 0, tmp, 1, size);
        array = tmp;
        size = size + 1;
    }

    @Override
    public void addLast(T item) {
        if (size == capacity) {
            capacity = 2 * capacity;
            T[] tmp = (T[]) new Object[capacity];
            System.arraycopy(array, 0, tmp, 0, size);
            array = tmp;
        }
        array[size] = item;
        size = size + 1;
    }


//    public boolean isEmpty() {
//        /* Returns true if deque is empty, false otherwise. */
//    }

    public int size() {
        return size;
    }

    public void printDeque() {
        /* Prints the items in the deque from first to last, separated by a space.
        Once all the items have been printed, print out a new line.
         */
        for (int i=0; i<size; i++) {
            System.out.print(array[i] + " ");
        }
        System.out.println();
    }

    public T removeFirst() {
        /* Removes and returns the item at the front of the deque.
        If no such item exists, returns null.
         */
        if (size == 0) {
            return null;
        }
        if (size < capacity/4) {
            capacity = capacity/4;
        }
        T[] tmp = (T[]) new Object[capacity];
        System.arraycopy(array, 1, tmp, 0, size-1);
        size = size - 1;
        T rm = array[0];
        array = tmp;
        return rm;
    }

    public T removeLast() {
        if (size == 0) {
            return null;
        }
        if (size < capacity/4) {
            capacity = capacity/4;
        }
        T[] tmp = (T[]) new Object[capacity];
        System.arraycopy(array, 0, tmp, 0, size-1);
        size = size - 1;
        T rm = array[size];
        array = tmp;
        return rm;
    }

    public T get(int index) {
        /* Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
        If no such item exists, returns null.
        Must not alter the deque!
         */
        if (index < 0 || index >= size) {
            return null;
        }
        return array[index];
    }

}