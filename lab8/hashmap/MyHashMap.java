package hashmap;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private double load;
    private double maxLoad;
    private int maxSize;

    /** Constructors */
    public MyHashMap() {
        maxSize = 16;
        maxLoad = 0.75;
        size = 0;
        load = 0;
        buckets = createTable(maxSize);
    }

    public MyHashMap(int initialSize) {
        maxSize = initialSize;
        maxLoad = 0.75;
        size = 0;
        load = 0;
        buckets = createTable(maxSize);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        maxSize = initialSize;
        this.maxLoad = maxLoad;
        size = 0;
        load = 0;
        buckets = createTable(maxSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new HashSet<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    private void resize(int tabelSize) {
        Collection<Node>[] tabel = createTable(tabelSize);
        for (K key : keySet()) {
            int idx = Math.floorMod(key.hashCode(), tabelSize);
            tabel[idx].add(new Node(key, get(key)));
        }
        buckets = tabel;
    }

    @Override
    public void clear() {
        buckets = createTable(maxSize);
        size = 0;
        load = 0.0;
    }

    @Override
    public boolean containsKey(K key) {
        return keySet().contains(key);
    }

    @Override
    public V get(K key) {
        int idx = Math.floorMod(key.hashCode(), maxSize);
        Iterator<Node> iter = buckets[idx].iterator();
        while (iter.hasNext()) {
            Node tmp = iter.next();
            if (key.equals(tmp.key)) {
                return tmp.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private void updateParam() {
        size = size + 1;
        load = (size + 0.0) / maxSize;
        if (load > maxLoad) {
            resize(2 * maxSize);
            maxSize = 2 * maxSize;
            load = (size + 0.0) / maxSize;
        }
    }

    @Override
    public void put(K key, V value) {
        int idx = Math.floorMod(key.hashCode(), maxSize);
        if (containsKey(key)) {
            remove(key);
        }
        buckets[idx].add(new Node(key, value));
        updateParam();
    }

    @Override
    public Set<K> keySet() {
        Set<K> kset = new HashSet<>();
        Iterator<K> iter = iterator();
        while (iter.hasNext()) {
            kset.add(iter.next());
        }
        return kset;
    }

    @Override
    public V remove(K key) {
        if (!containsKey(key)) {
            return null;
        }
        int idx = Math.floorMod(key.hashCode(), maxSize);
        Iterator<Node> iter = buckets[idx].iterator();
        Node tmp = null;
        while (iter.hasNext()) {
            tmp = iter.next();
            if (key.equals(tmp.key)) {
                buckets[idx].remove(tmp);
                break;
            }
            tmp = null;
        }
        if (tmp == null) {
            return null;
        }
        size = size - 1;
        load = (size + 0.0) / maxSize;
        return tmp.value;
    }

    @Override
    public V remove(K key, V value) {
        if (!containsKey(key)) {
            return null;
        }
        int idx = Math.floorMod(key.hashCode(), maxSize);
        Iterator<Node> iter = buckets[idx].iterator();
        Node tmp = null;
        while (iter.hasNext()) {
            tmp = iter.next();
            if (key.equals(tmp.key) && value.equals(tmp.value)) {
                buckets[idx].remove(tmp);
                break;
            }
            tmp = null;
        }
        if (tmp == null) {
            return null;
        }
        size = size - 1;
        load = (size + 0.0) / maxSize;
        return tmp.value;
    }

    private class HashMapIterator implements Iterator<K> {
        private int pos = 0;
        private Iterator<Node> iter = null;
        private int outNum = 0;
        private boolean initOver = false;

        private void init() {
            pos = 0;
            while (buckets[pos] == null) {
                pos = pos + 1;
                if (pos >= maxSize) {
                    return;
                }
            }
            iter = buckets[pos].iterator();
            outNum = 0;
            initOver = true;
        }

        @Override
        public boolean hasNext() {
            return outNum < size;
        }

        @Override
        public K next() {
            if (!initOver) {
                init();
            }
            while (!iter.hasNext() && hasNext()) {
                pos = pos + 1;
                iter = buckets[pos].iterator();
            }
            if (!hasNext()) {
                return null;
            }
            outNum = outNum + 1;
            return iter.next().key;
        }
    }

    private Iterator<K> getIterator() {
        return new HashMapIterator();
    }

    @Override
    public Iterator<K> iterator() {
        return getIterator();
    }

}
