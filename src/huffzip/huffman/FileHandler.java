package huffzip.huffman;

import huffzip.utils.LoggerUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * FileHandler.java
 * ----------------
 * Handles reading/writing the custom compressed file format (.huf) and
 * the low-level bit-packing needed to turn a bitstring into real bytes.
 *
 * File format (all integers big-endian):
 *   [4 bytes]  magic header b"HUFZ"
 *   [1 byte]   padding bits added to the final byte (0-7)
 *   [4 bytes]  number of entries in the frequency table
 *   [table]    for each entry: 1 byte (symbol) + 4 bytes (frequency)
 *   [payload]  the packed, encoded bitstream
 */
public final class FileHandler {

    public static final byte[] MAGIC = {'H', 'U', 'F', 'Z'};

    private FileHandler() {
        // Utility class; not instantiable.
    }

    /** Result of packing a bitstring: the packed bytes plus how many padding bits were added. */
    public static final class PackedResult {
        public final byte[] packed;
        public final int padding;

        public PackedResult(byte[] packed, int padding) {
            this.packed = packed;
            this.padding = padding;
        }
    }

    /** Pack a '0'/'1' string into real bytes, right-padding with zeros. */
    public static PackedResult bitstringToBytes(String bitString) {
        int padding = (8 - bitString.length() % 8) % 8;
        StringBuilder padded = new StringBuilder(bitString);
        for (int i = 0; i < padding; i++) {
            padded.append('0');
        }
        int numBytes = padded.length() / 8;
        byte[] out = new byte[numBytes];
        for (int i = 0; i < numBytes; i++) {
            String byteStr = padded.substring(i * 8, (i + 1) * 8);
            out[i] = (byte) Integer.parseInt(byteStr, 2);
        }
        return new PackedResult(out, padding);
    }

    /** Reverse of bitstringToBytes: unpack bytes back into a bit string. */
    public static String bytesToBitstring(byte[] data, int padding) {
        StringBuilder sb = new StringBuilder(data.length * 8);
        for (byte b : data) {
            String bits = Integer.toBinaryString(b & 0xFF);
            for (int i = bits.length(); i < 8; i++) {
                sb.append('0');
            }
            sb.append(bits);
        }
        String result = sb.toString();
        if (padding > 0) {
            result = result.substring(0, result.length() - padding);
        }
        return result;
    }

    /** Write the magic header, frequency table, and packed payload to disk. */
    public static void writeCompressedFile(String path, Map<Integer, Integer> freqTable, String bitString)
            throws IOException {
        PackedResult packedResult = bitstringToBytes(bitString);
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(path)))) {
            out.write(MAGIC);
            out.writeByte(packedResult.padding);
            out.writeInt(freqTable.size());
            for (Map.Entry<Integer, Integer> entry : freqTable.entrySet()) {
                out.writeByte(entry.getKey());
                out.writeInt(entry.getValue());
            }
            out.write(packedResult.packed);
        } catch (IOException exc) {
            throw new IOException("Failed to write compressed file to '" + path + "': " + exc.getMessage(), exc);
        }
        LoggerUtil.info("Wrote compressed file: " + path);
    }

    /** Result of reading a .huf file: the frequency table plus the decoded bitstring. */
    public static final class ReadResult {
        public final Map<Integer, Integer> freqTable;
        public final String bitString;

        public ReadResult(Map<Integer, Integer> freqTable, String bitString) {
            this.freqTable = freqTable;
            this.bitString = bitString;
        }
    }

    /** Read a .huf file and return its frequency table and bitstring. */
    public static ReadResult readCompressedFile(String path) throws IOException {
        byte[] magic = new byte[4];
        int padding;
        int tableSize;
        Map<Integer, Integer> freqTable = new LinkedHashMap<>();
        byte[] packed;

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            in.readFully(magic);
            if (!Arrays.equals(magic, MAGIC)) {
                throw new IOException("Invalid or corrupted compressed file (unexpected header).");
            }
            padding = in.readUnsignedByte();
            tableSize = in.readInt();
            for (int i = 0; i < tableSize; i++) {
                int symbol = in.readUnsignedByte();
                int freq = in.readInt();
                freqTable.put(symbol, freq);
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) != -1) {
                buffer.write(chunk, 0, n);
            }
            packed = buffer.toByteArray();
        } catch (EOFException exc) {
            throw new IOException("Corrupted compressed file (unexpected end of file): " + path, exc);
        } catch (IOException exc) {
            throw new IOException("Failed to read compressed file '" + path + "': " + exc.getMessage(), exc);
        }

        String bitString = bytesToBitstring(packed, padding);
        return new ReadResult(freqTable, bitString);
    }
}
