package structures;

import java.util.TreeMap;

public class BTree<T extends Comparable<T>> {
    private final int t; // Minimum degree (document choice in Report Q2)
    // TODO (Person 1): Replace stubMap with actual B-Tree root node and splitting logic
    private final TreeMap<T, T> stubMap = new TreeMap<>();

    public BTree(int t) {
        this.t = t;
    }

    public void insert(T item) {
        stubMap.put(item, item);
    }

    public T search(T key) {
        return stubMap.get(key);
    }

    public int getDegree() {
        return t;
    }
}