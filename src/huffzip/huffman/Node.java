package huffzip.huffman;

/**
 * Node.java
 * ---------
 * Defines the Node data structure used to build the Huffman binary tree.
 *
 * Each node either represents a single byte value (a leaf) with its
 * frequency of occurrence, or an internal node that merges two subtrees
 * (with the combined frequency of its children).
 */
public class Node implements Comparable<Node> {

    /** Byte value (0-255) for leaf nodes, null for internal nodes. */
    public final Integer symbol;

    /** Frequency / combined frequency of this subtree. */
    public final int freq;

    /** Left child (Node or null). */
    public final Node left;

    /** Right child (Node or null). */
    public final Node right;

    public Node(Integer symbol, int freq, Node left, Node right) {
        this.symbol = symbol;
        this.freq = freq;
        this.left = left;
        this.right = right;
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    @Override
    public int compareTo(Node other) {
        // Required so PriorityQueue can order Nodes by frequency (min-heap).
        return Integer.compare(this.freq, other.freq);
    }

    @Override
    public String toString() {
        return "Node(symbol=" + symbol + ", freq=" + freq + ")";
    }
}
