package structures;

/**
 * Stores the data belonging to a single B-Tree node.
 *
 * @param <T> the type of the record being stored in the node
 */
class BTreeNode<T extends Comparable<T>> {
    final Object[] keys;
    final BTreeNode<T>[] children;

    int keyCount;
    final boolean isLeaf;

    /**
    * Creates a new empty B-Tree node.
    *
    * @param minimumDegree the minimum degree of this node
    * @param isLeaf true if this node has no children
    */
    @SuppressWarnings("unchecked")
    BTreeNode(int minimumDegree, boolean isLeaf) {
        this.keys = new Object[(2 * minimumDegree) - 1];
        this.children = (BTreeNode<T>[]) new BTreeNode<?>[2 * minimumDegree];
        this.keyCount = 0;
        this.isLeaf = isLeaf;
    }

    /**
    * Returns the record stored in the requested index in the node.
    * 
    * @param index the position of the record in the node
    * @return the record at that position
    */
    @SuppressWarnings("unchecked")
    T getKey(int index) {
        return (T) keys[index];
    }
}