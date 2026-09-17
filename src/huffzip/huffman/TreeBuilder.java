package huffzip.huffman;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * TreeBuilder.java
 * ----------------
 * Core DSA logic: builds a frequency table for the input data and
 * constructs the Huffman tree using a min-heap (PriorityQueue) based
 * greedy algorithm.
 *
 * Time complexity:
 *   - Frequency table construction: O(n)
 *   - Huffman tree construction:    O(k log k), where k = number of
 *     distinct byte values (k &lt;= 256 for byte data)
 */
public final class TreeBuilder {

    private TreeBuilder() {
        // Utility class; not instantiable.
    }

    /** Return a map from each byte value (0-255) to its number of occurrences. */
    public static Map<Integer, Integer> buildFrequencyTable(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("buildFrequencyTable expects a non-null byte array.");
        }
        Map<Integer, Integer> table = new LinkedHashMap<>();
        for (byte b : data) {
            int value = b & 0xFF; // unsigned byte value, 0-255
            table.merge(value, 1, Integer::sum);
        }
        return table;
    }

    /**
     * Build a Huffman tree from a frequency table using a min-heap.
     *
     * Greedy strategy: repeatedly pop the two least-frequent nodes and
     * merge them into a new internal node, until only the root remains.
     */
    public static Node buildHuffmanTree(Map<Integer, Integer> freqTable) {
        if (freqTable == null || freqTable.isEmpty()) {
            throw new IllegalArgumentException("Cannot build a Huffman tree from an empty frequency table.");
        }

        PriorityQueue<Node> heap = new PriorityQueue<>();
        for (Map.Entry<Integer, Integer> entry : freqTable.entrySet()) {
            heap.add(new Node(entry.getKey(), entry.getValue(), null, null));
        }

        // Special case: only one distinct symbol in the input.
        if (heap.size() == 1) {
            Node only = heap.poll();
            return new Node(null, only.freq, only, null);
        }

        while (heap.size() > 1) {
            Node left = heap.poll();
            Node right = heap.poll();
            Node merged = new Node(null, left.freq + right.freq, left, right);
            heap.add(merged);
        }

        return heap.poll();
    }
}
