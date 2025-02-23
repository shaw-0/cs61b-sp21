package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private class BSTNode<K, V> {
        private K key;
        private V value;
        private BSTNode<K,V> left;
        private BSTNode<K,V> right;

        public BSTNode(K k, V v) {
            key = k;
            value = v;
            left = null;
            right = null;
        }

        public void setLeft(BSTNode<K,V> left) {
            this.left = left;
        }

        public void setRight(BSTNode<K,V> right) {
            this.right = right;
        }

        public BSTNode<K,V> getLeft() {
            return this.left;
        }

        public BSTNode<K,V> getRight() {
            return this.right;
        }

        public V getValue() {
            return this.value;
        }

        public K getKey() {
            return this.key;
        }
    }

    private int size;
    private BSTNode<K,V> top;

    public BSTMap() {
        size = 0;
        top = null;
    }

    private BSTNode<K,V> insert(K key, V value, BSTNode<K,V> map) {
        if (map == null) {
            map = new BSTNode<>(key, value);
        } else if (key.compareTo(map.getKey()) < 0) {
            map.setLeft(insert(key, value, map.getLeft()));
        } else if (key.compareTo(map.getKey()) > 0) {
            map.setRight(insert(key, value, map.getRight()));
        }
        return map;
    }

    private boolean contains(BSTNode<K,V> map, K key) {
        if (key == null || map == null) {
            return false;
        }
        if (key.compareTo(map.getKey()) == 0) {
            return true;
        } else if (key.compareTo(map.getKey()) < 0) {
            return contains(map.getLeft(), key);
        } else {
            return contains(map.getRight(), key);
        }
    }

    private V find(BSTNode<K,V> map, K key) {
        if (map == null || key == null) {
            return null;
        }
        if (key.compareTo(map.getKey()) == 0) {
            return map.getValue();
        } else if(key.compareTo(map.getKey()) < 0) {
            return find(map.getLeft(), key);
        } else {
            return find(map.getRight(), key);
        }
    }

    private void printMap(BSTNode<K,V> node) {
        if (node == null) {
            return;
        }
        printMap(node.getLeft());
        printNode(node);
        printMap(node.getRight());

    }

    private void printNode(BSTNode<K,V> node) {
        if (node == null) {
            return;
        }
        System.out.print(node.getKey() + ": " + node.getValue() + "; ");
    }

    @Override
    public void clear() {
        /** Removes all of the mappings from this map. */
        top = null;
        size = 0;
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        return contains(top, key);
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        if (containsKey(key)) {
            return find(top, key);
        }
        return null;
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        if (top == null) {
            top = new BSTNode<>(key, value);
            size = size + 1;
        } else if (!this.containsKey(key)) {
            top = insert(key, value, top);
            size = size + 1;
        }
    }

    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException("ketSet not finished......");
    }

    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException("remove key not finished......");
    }

    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException("remove key & value not finished......");
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException("iterator not finished......");
    }

    public void printInOrder() {
        if (top == null) {
            return;
        }
        printMap(top);
        System.out.println();
    }
}
