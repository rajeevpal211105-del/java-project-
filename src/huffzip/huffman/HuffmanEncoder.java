package huffzip.huffman;

import huffzip.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * HuffmanEncoder.java
 * -------------------
 * Generates prefix-free binary codes from a Huffman tree (via DFS
 * tree traversal) and encodes raw byte data into a bitstring.
 */
public class HuffmanEncoder {

    private final byte[] data;
    private final Map<Integer, Integer> freqTable;
    private final Node treeRoot;
    private final Map<Integer, String> codeMap;

    public HuffmanEncoder(byte[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("HuffmanEncoder cannot encode empty data.");
        }
        this.data = data;
        this.freqTable = TreeBuilder.buildFrequencyTable(data);
        this.treeRoot = TreeBuilder.buildHuffmanTree(freqTable);
        this.codeMap = buildCodes(treeRoot, "", new HashMap<>());
        LoggerUtil.info("Generated " + codeMap.size() + " unique Huffman codes.");
    }

    /**
     * Recursively traverse the Huffman tree (DFS) to build a mapping of
     * byte value -&gt; binary code string. Left edges = '0', right = '1'.
     */
    public static Map<Integer, String> buildCodes(Node node, String prefix, Map<Integer, String> codeMap) {
        if (node == null) {
            return codeMap;
        }
        if (node.isLeaf()) {
            // Edge case: tree with a single symbol still needs a valid code.
            codeMap.put(node.symbol, prefix.isEmpty() ? "0" : prefix);
            return codeMap;
        }
        buildCodes(node.left, prefix + "0", codeMap);
        buildCodes(node.right, prefix + "1", codeMap);
        return codeMap;
    }

    /** Translate raw bytes into a single bitstring using the code map. */
    public static String encodeData(byte[] data, Map<Integer, String> codeMap) {
        StringBuilder sb = new StringBuilder();
        for (byte b : data) {
            int value = b & 0xFF;
            String code = codeMap.get(value);
            if (code == null) {
                throw new IllegalStateException("Byte value " + value + " has no assigned Huffman code.");
            }
            sb.append(code);
        }
        return sb.toString();
    }

    /** Result of an encode() call: the bitstring plus the frequency table needed to decode it later. */
    public static final class EncodeResult {
        public final String bitString;
        public final Map<Integer, Integer> freqTable;

        public EncodeResult(String bitString, Map<Integer, Integer> freqTable) {
            this.bitString = bitString;
            this.freqTable = freqTable;
        }
    }

    /** Return the bitstring and freq table needed to reconstruct the tree later. */
    public EncodeResult encode() {
        String bitString = encodeData(data, codeMap);
        return new EncodeResult(bitString, freqTable);
    }
}
