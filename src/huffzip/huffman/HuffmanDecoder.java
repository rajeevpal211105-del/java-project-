package huffzip.huffman;

import huffzip.utils.LoggerUtil;

import java.util.Map;

/**
 * HuffmanDecoder.java
 * -------------------
 * Rebuilds the Huffman tree from a stored frequency table and decodes
 * a bitstring back into the original byte sequence by walking the tree.
 */
public class HuffmanDecoder {

    private final Map<Integer, Integer> freqTable;
    private final Node treeRoot;

    public HuffmanDecoder(Map<Integer, Integer> freqTable) {
        if (freqTable == null || freqTable.isEmpty()) {
            throw new IllegalArgumentException("Cannot decode: frequency table is empty or missing.");
        }
        this.freqTable = freqTable;
        this.treeRoot = TreeBuilder.buildHuffmanTree(freqTable);
    }

    /** Walk the tree bit-by-bit, emitting a byte each time a leaf is hit. */
    public byte[] decode(String bitString) {
        Node node = treeRoot;

        // Edge case: original data had only one distinct byte value.
        if (node.isLeaf()) {
            int count = freqTable.get(node.symbol);
            byte[] out = new byte[count];
            java.util.Arrays.fill(out, node.symbol.byteValue());
            return out;
        }

        byte[] buffer = new byte[bitString.length()];
        int size = 0;

        for (int i = 0; i < bitString.length(); i++) {
            char bit = bitString.charAt(i);
            node = (bit == '0') ? node.left : node.right;
            if (node == null) {
                throw new IllegalStateException("Corrupted bitstream: invalid path in Huffman tree.");
            }
            if (node.isLeaf()) {
                buffer[size++] = node.symbol.byteValue();
                node = treeRoot;
            }
        }

        byte[] result = java.util.Arrays.copyOf(buffer, size);
        LoggerUtil.info("Decoded " + result.length + " bytes from bitstream.");
        return result;
    }
}
