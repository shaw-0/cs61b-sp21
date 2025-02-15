package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {
    private int size;
    private ListNode<T> sentinel;
    private ListNode<T> pointer;

    public class ListNode<T> {
        private T num;
        private ListNode<T> before;
        private ListNode<T> after;

        public ListNode() {
            before = null;
            after = null;
        }

        public void setBefore(ListNode<T> node) {
            before = node;
        }

        public void setAfter(ListNode<T> node) {
            after = node;
        }

        public void setNum(T n) {
            num = n;
        }

        public ListNode<T> getBefore() {
            return before;
        }

        public ListNode<T> getAfter() {
            return after;
        }

        public T getNum() {
            return num;
        }

    }

    public LinkedListDeque() {
        /* create an empty list deque*
         *
         */
        sentinel = new ListNode<>();
        sentinel.setBefore(sentinel);
        sentinel.setAfter(sentinel);
        size = 0;
        pointer = sentinel;
    }

    /**
     * Add method:
     * must not involve any looping or recursion.
     * A single such operation must take “constant time”.
     * i.e. execution time should not depend on the size of the deque.
     */
    @Override
    public void addFirst(T item) {
        /* Adds an item of type T to the front of the deque.
        You can assume that item is never null.
        */
        ListNode<T> add = new ListNode<>();
        add.setNum(item);
        add.setBefore(sentinel);
        add.setAfter(sentinel.getAfter());
        sentinel.setAfter(add);
        add.getAfter().setBefore(add);
        size = size + 1;
    }

    @Override
    public void addLast(T item) {
        ListNode<T> add = new ListNode<>();
        add.setNum(item);
        add.setBefore(sentinel.getBefore());
        add.setAfter(sentinel);
        sentinel.setBefore(add);
        add.getBefore().setAfter(add);
        size = size + 1;
    }

//    @Override
//    public boolean isEmpty() {
//        /* Returns true if deque is empty, false otherwise. */
//    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void printDeque() {
        /* Prints the items in the deque from first to last, separated by a space.
        Once all the items have been printed, print out a new line.
         */
        ListNode<T> node = sentinel;
        for (int i = 0; i < size; i++) {
            node = node.getAfter();
            System.out.print(node.getNum() + " ");
        }
        System.out.println();
    }

    @Override
    public T removeFirst() {
        /* Removes and returns the item at the front of the deque.
        If no such item exists, returns null.
         */
        if (size == 0) {
            return null;
        }
        ListNode<T> rm = sentinel.getAfter();
        ListNode<T> f = rm.getAfter();
        sentinel.setAfter(f);
        f.setBefore(sentinel);
        size = size - 1;
        return rm.getNum();
    }

    @Override
    public T removeLast() {
        if (size == 0) {
            return null;
        }
        ListNode<T> rm = sentinel.getBefore();
        ListNode<T> l = rm.getBefore();
        sentinel.setBefore(l);
        l.setAfter(sentinel);
        size = size - 1;
        return rm.getNum();
    }

    @Override
    public T get(int index) {
        /* Gets the item at the given index, where 0 is the front, 1 is the next item, and so forth.
        If no such item exists, returns null.
        Must not alter the deque!
         */
        if (index >= size || index < 0) {
            return null;
        }
        ListNode<T> node = sentinel;
        node = node.getAfter();
        for (int i = 0; i < index; i++) {
            node = node.getAfter();
        }
        return node.getNum();
    }

//    public Iterator<T> iterator() {
//
//    }

    @Override
    public boolean equals(Object o) {
        /* Returns whether or not the parameter o is equal to the Deque.
         o is considered equal if it is a Deque and if it contains the same contents
         (as goverened by the generic T’s equals method) in the same order.
         (ADDED 2/12: You’ll need to use the instance of keywords for this.)
         */
        if (o instanceof LinkedListDeque lld) {
            if (lld.size() != size) {
                return false;
            }
            for (int i = 0; i < size; i++) {
                pointer = pointer.getAfter();
                if (!pointer.getNum().equals(lld.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public T getRecursive(int index) {
        /* Same as get, but uses recursion.
         *
         */
        if (index >= size || index < 0) {
            return null;
        }
        if (index == 0) {
            T res = pointer.getAfter().getNum();
            pointer = sentinel;
            return res;
        }
        pointer = pointer.getAfter();
        return getRecursive(index - 1);
    }

    private class LLiterator implements Iterator<T> {
        private ListNode<T> node;

        public LLiterator() {
            node = sentinel;
        }

        public boolean hasNext() {
            return node.getAfter() != sentinel;
        }

        public T next() {
            node = node.getAfter();
            return node.getNum();
        }
    }

    @Override
    public Iterator<T> iterator() {
        return new LLiterator();
    }
}
