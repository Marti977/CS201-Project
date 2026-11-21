package structures;

import java.util.TreeMap;

/**
 * A B-Tree that stores records.
 */
public class BTree<T extends Comparable<T>> {
    private final int minimumDegree;
    private BTreeNode<T> root;
    private final TreeMap<T, T> stubMap = new TreeMap<>();

    /**
    * Creates a new empty B-Tree with the specified minimumDegree
    *
    * @param minimumDegree the minimum degree, must be greater than or equal to 2
    * @throws IllegalArgumentException if minimumDegree is less than 2
    */
    public BTree(int minimumDegree) {
        if (minimumDegree < 2) {
            throw new IllegalArgumentException("'minimumDegree' must be at least '2'");
        }

        this.minimumDegree = minimumDegree;
        this.root = new BTreeNode<>(minimumDegree, true);
    }

    /**
     * Inserts a record into the tree. If an existing record with the key already exists,
     * it is overwritten.
     * 
     * @param item the record to be inserted
     * @throws IllegalArgumentException if item is null or invalid
     */
    public void insert(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot insert a 'null' record.");
        }

        if (replaceIfPresent(root, item)) {
            return;
        }

        if (root.keyCount == (2 * minimumDegree) - 1) {
            BTreeNode<T> newRoot = new BTreeNode<>(minimumDegree, false);
            newRoot.children[0] = root;

            splitChild(newRoot, 0);
            root = newRoot;
        }

        insertNonFull(root, item);
    }

    /**
     * Searches for a record with the matching key as the given record
     * 
     * @param item a record containing the key to be searched for
     * @return the stored record, or null if no existing matching record is found
     * @throws IllegalArgumentException if 'item' is 'null' or invalid
     */
    public T search(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot search for a 'null' key.");
        }

        return searchNode(root, item);
    }

    /**
     * Returns the minimum degree used by this tree.
     *
     * @return the minimum degree
     */
    public int getDegree() {
        return minimumDegree;
    }

    /**
     * Searches a node and, when needed, the appropriate child.
     *
     * @param node the current node
     * @param item the record whose key is being searched for
     * @return the matching stored record, or null if it is absent
     */
    private T searchNode(BTreeNode<T> node, T item) {
        int index = 0;

        while (index < node.keyCount && item.compareTo(node.getKey(index)) > 0) {
            index++;
        }

        if (index < node.keyCount && item.compareTo(node.getKey(index)) == 0) {
            return node.getKey(index);
        }

        // A leaf has no children left to search.
        if (node.isLeaf) {
            return null;
        }

        return searchNode(node.children[index], item);
    }

    /**
     * Replaces a record if its key matches the given record.
     *
     * @param node the current node
     * @param item the new record
     * @return true if an existing record was replaced
     */
    private boolean replaceIfPresent(BTreeNode<T> node, T item) {
        int index = 0;

        while (index < node.keyCount && item.compareTo(node.getKey(index)) > 0) {
            index++;
        }

        if (index < node.keyCount && item.compareTo(node.getKey(index)) == 0) {
            node.keys[index] = item;
            return true;
        }

        if (node.isLeaf) {
            return false;
        }

        return replaceIfPresent(node.children[index], item);
    }

    /**
     * Inserts a new record into a node that is known to have available space.
     *
     * @param node the non-full node to insert into
     * @param item the record to insert
     */
    private void insertNonFull(BTreeNode<T> node, T item) {
        int index = node.keyCount - 1;

        if (node.isLeaf) {
            while (index >= 0 && item.compareTo(node.getKey(index)) < 0) {
                node.keys[index + 1] = node.keys[index];
                index--;
            }

            node.keys[index + 1] = item;
            node.keyCount++;
            return;
        }

        while (index >= 0 && item.compareTo(node.getKey(index)) < 0) {
            index--;
        }
        index++;

        if (node.children[index].keyCount == (2 * minimumDegree) - 1) {
            splitChild(node, index);

            if (item.compareTo(node.getKey(index)) > 0) {
                index++;
            }
        }

        insertNonFull(node.children[index], item);
    }

    /**
     * Splits a full child into two nodes and promotes its middle record.
     *
     * @param parent the non-full parent of the child being split
     * @param childIndex the position of the full child in the parent
     */
    private void splitChild(BTreeNode<T> parent, int childIndex) {
        BTreeNode<T> fullChild = parent.children[childIndex];
        BTreeNode<T> rightChild = new BTreeNode<>(minimumDegree, fullChild.isLeaf);

        T middleKey = fullChild.getKey(minimumDegree - 1);
        rightChild.keyCount = minimumDegree - 1;

        for (int i = 0; i < minimumDegree - 1; i++) {
            rightChild.keys[i] = fullChild.keys[i + minimumDegree];
            fullChild.keys[i + minimumDegree] = null;
        }

        if (!fullChild.isLeaf) {
            for (int i = 0; i < minimumDegree; i++) {
                rightChild.children[i] = fullChild.children[i + minimumDegree];
                fullChild.children[i + minimumDegree] = null;
            }
        }

        fullChild.keys[minimumDegree - 1] = null;
        fullChild.keyCount = minimumDegree - 1;

        for (int i = parent.keyCount; i >= childIndex + 1; i--) {
            parent.children[i + 1] = parent.children[i];
        }
        
        parent.children[childIndex + 1] = rightChild;

        for (int i = parent.keyCount - 1; i >= childIndex; i--) {
            parent.keys[i + 1] = parent.keys[i];
        }

        parent.keys[childIndex] = middleKey;
        parent.keyCount++;
    }
}