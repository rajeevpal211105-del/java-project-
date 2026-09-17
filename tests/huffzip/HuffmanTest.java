package huffzip;

import huffzip.huffman.FileHandler;
import huffzip.huffman.HuffmanDecoder;
import huffzip.huffman.HuffmanEncoder;
import huffzip.huffman.Node;
import huffzip.huffman.TreeBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * HuffmanTest.java
 * ----------------
 * Dependency-free unit test suite for the HuffZip core (no JUnit —
 * the project's "no external dependencies" requirement extends to
 * testing, so it builds and runs with only the JDK).
 *
 * Covers frequency counting, prefix-code correctness, full encode/decode
 * round trips (including edge cases: empty files, single-repeated-byte
 * files, binary data), and the bit-packing helpers — mirroring the
 * original 9-test suite.
 *
 * Run with:
 *   java -cp out huffzip.HuffmanTest
 */
public final class HuffmanTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        test("Frequency table counts are correct", HuffmanTest::testFrequencyCounts);
        test("Frequency table raises on empty table", HuffmanTest::testEmptyFreqTableRaises);
        test("Huffman codes are prefix-free", HuffmanTest::testPrefixFreeCodes);
        test("Round trip: normal text", HuffmanTest::testRoundTripNormalText);
        test("Round trip: binary data (all 256 byte values)", HuffmanTest::testRoundTripBinaryData);
        test("Round trip: single repeated character", HuffmanTest::testRoundTripSingleChar);
        test("Empty data is rejected by encoder", HuffmanTest::testEmptyDataRejected);
        test("Skewed distribution compresses well", HuffmanTest::testSkewedCompressesWell);
        test("Bit packing round trip (various lengths)", HuffmanTest::testBitPackingRoundTrip);

        System.out.println();
        System.out.println(passed + " passed, " + failed + " failed");
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---- test cases ---------------------------------------------------

    private static void testFrequencyCounts() {
        byte[] data = "aaabbc".getBytes();
        Map<Integer, Integer> table = TreeBuilder.buildFrequencyTable(data);
        assertEquals(table.get((int) 'a'), 3);
        assertEquals(table.get((int) 'b'), 2);
        assertEquals(table.get((int) 'c'), 1);
    }

    private static void testEmptyFreqTableRaises() {
        boolean threw = false;
        try {
            TreeBuilder.buildHuffmanTree(new HashMap<>());
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "Expected IllegalArgumentException on empty frequency table");
    }

    private static void testPrefixFreeCodes() {
        byte[] data = "the quick brown fox jumps over the lazy dog".getBytes();
        Node root = TreeBuilder.buildHuffmanTree(TreeBuilder.buildFrequencyTable(data));
        Map<Integer, String> codes = HuffmanEncoder.buildCodes(root, "", new HashMap<>());
        List<String> codeList = new ArrayList<>(codes.values());
        for (int i = 0; i < codeList.size(); i++) {
            for (int j = 0; j < codeList.size(); j++) {
                if (i == j) continue;
                assertTrue(!codeList.get(j).startsWith(codeList.get(i)),
                        "Code " + codeList.get(i) + " is a prefix of " + codeList.get(j));
            }
        }
    }

    private static void testRoundTripNormalText() {
        roundTripCheck("The quick brown fox jumps over the lazy dog. 1234567890!@#$%".getBytes());
    }

    private static void testRoundTripBinaryData() {
        byte[] data = new byte[256];
        for (int i = 0; i < 256; i++) {
            data[i] = (byte) i;
        }
        roundTripCheck(data);
    }

    private static void testRoundTripSingleChar() {
        byte[] data = new byte[100];
        Arrays.fill(data, (byte) 'x');
        roundTripCheck(data);
    }

    private static void testEmptyDataRejected() {
        boolean threw = false;
        try {
            new HuffmanEncoder(new byte[0]);
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "Expected IllegalArgumentException on empty data");
    }

    private static void testSkewedCompressesWell() {
        StringBuilder sb = new StringBuilder();
        Random random = new Random(42);
        for (int i = 0; i < 1000; i++) {
            sb.append(random.nextInt(100) < 90 ? 'a' : (char) ('b' + random.nextInt(5)));
        }
        byte[] data = sb.toString().getBytes();
        HuffmanEncoder encoder = new HuffmanEncoder(data);
        HuffmanEncoder.EncodeResult result = encoder.encode();
        int compressedBytesApprox = (result.bitString.length() + 7) / 8;
        assertTrue(compressedBytesApprox < data.length,
                "Compressed size should be smaller than original for a skewed distribution");
    }

    private static void testBitPackingRoundTrip() {
        int[] lengths = {0, 1, 7, 8, 9, 15, 16, 17, 100};
        for (int len : lengths) {
            StringBuilder sb = new StringBuilder();
            Random random = new Random(len + 1);
            for (int i = 0; i < len; i++) {
                sb.append(random.nextBoolean() ? '1' : '0');
            }
            String bits = sb.toString();
            FileHandler.PackedResult packed = FileHandler.bitstringToBytes(bits);
            String unpacked = FileHandler.bytesToBitstring(packed.packed, packed.padding);
            assertEquals(unpacked, bits);
        }
    }

    // ---- round-trip helper ---------------------------------------------

    private static void roundTripCheck(byte[] data) {
        HuffmanEncoder encoder = new HuffmanEncoder(data);
        HuffmanEncoder.EncodeResult result = encoder.encode();
        HuffmanDecoder decoder = new HuffmanDecoder(result.freqTable);
        byte[] decoded = decoder.decode(result.bitString);
        assertTrue(Arrays.equals(data, decoded), "Round-trip data mismatch");
    }

    // ---- tiny test harness ----------------------------------------------

    private interface TestCase {
        void run() throws Exception;
    }

    private static void test(String name, TestCase testCase) {
        try {
            testCase.run();
            System.out.println("[PASS] " + name);
            passed++;
        } catch (Throwable e) {
            System.out.println("[FAIL] " + name + " -> " + e);
            failed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionError("Expected " + expected + " but got " + actual);
        }
    }
}
