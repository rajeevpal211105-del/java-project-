package huffzip;

import huffzip.huffman.FileHandler;
import huffzip.huffman.HuffmanDecoder;
import huffzip.huffman.HuffmanEncoder;
import huffzip.utils.LoggerUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Main.java
 * ---------
 * Command-line entry point for HuffZip, a Huffman-coding based file
 * compression tool.
 *
 * Usage:
 *   java -cp out huffzip.Main compress   &lt;input_file&gt; &lt;output_file.huf&gt;
 *   java -cp out huffzip.Main decompress &lt;input_file.huf&gt; &lt;output_file&gt;
 *   java -cp out huffzip.Main stats      &lt;input_file&gt; &lt;compressed_file.huf&gt;
 *
 * Run with -h for full help.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            System.exit(1);
        }

        String command = args[0];
        switch (command) {
            case "-h":
            case "--help":
                printHelp();
                break;
            case "compress":
                cmdCompress(args);
                break;
            case "decompress":
                cmdDecompress(args);
                break;
            case "stats":
                cmdStats(args);
                break;
            default:
                System.err.println("Unknown command: " + command);
                printHelp();
                System.exit(1);
        }
    }

    private static void cmdCompress(String[] args) {
        if (args.length < 3 || isHelp(args)) {
            System.out.println("Usage: compress <input_file> <output_file.huf>");
            System.exit(args.length < 3 ? 1 : 0);
            return;
        }
        String inputPath = args[1];
        String outputPath = args[2];

        File inputFile = new File(inputPath);
        if (!inputFile.isFile()) {
            LoggerUtil.error("Input file not found: " + inputPath);
            System.exit(1);
        }

        byte[] data;
        try {
            data = Files.readAllBytes(inputFile.toPath());
        } catch (IOException exc) {
            LoggerUtil.error("Failed to read input file: " + exc.getMessage());
            System.exit(1);
            return;
        }

        if (data.length == 0) {
            LoggerUtil.error("Cannot compress an empty file: " + inputPath);
            System.exit(1);
        }

        try {
            HuffmanEncoder encoder = new HuffmanEncoder(data);
            HuffmanEncoder.EncodeResult result = encoder.encode();
            FileHandler.writeCompressedFile(outputPath, result.freqTable, result.bitString);
        } catch (IllegalArgumentException | IllegalStateException | IOException exc) {
            LoggerUtil.error("Compression failed: " + exc.getMessage());
            System.exit(1);
            return;
        }

        long originalSize = data.length;
        long compressedSize = new File(outputPath).length();
        double ratio = originalSize > 0 ? (1 - (double) compressedSize / originalSize) * 100 : 0;

        LoggerUtil.info("Compression complete.");
        LoggerUtil.info(String.format("  Original size:   %d bytes", originalSize));
        LoggerUtil.info(String.format("  Compressed size: %d bytes", compressedSize));
        LoggerUtil.info(String.format("  Space saved:     %.2f%%", ratio));
    }

    private static void cmdDecompress(String[] args) {
        if (args.length < 3 || isHelp(args)) {
            System.out.println("Usage: decompress <input_file.huf> <output_file>");
            System.exit(args.length < 3 ? 1 : 0);
            return;
        }
        String inputPath = args[1];
        String outputPath = args[2];

        if (!new File(inputPath).isFile()) {
            LoggerUtil.error("Input file not found: " + inputPath);
            System.exit(1);
        }

        try {
            FileHandler.ReadResult readResult = FileHandler.readCompressedFile(inputPath);
            HuffmanDecoder decoder = new HuffmanDecoder(readResult.freqTable);
            byte[] data = decoder.decode(readResult.bitString);
            Files.write(new File(outputPath).toPath(), data);
        } catch (IllegalArgumentException | IllegalStateException | IOException exc) {
            LoggerUtil.error("Decompression failed: " + exc.getMessage());
            System.exit(1);
            return;
        }

        LoggerUtil.info("Decompression complete. Output written to: " + outputPath);
    }

    private static void cmdStats(String[] args) {
        if (args.length < 3 || isHelp(args)) {
            System.out.println("Usage: stats <input_file> <compressed_file.huf>");
            System.exit(args.length < 3 ? 1 : 0);
            return;
        }
        String inputPath = args[1];
        String compressedPath = args[2];

        File inputFile = new File(inputPath);
        File compressedFile = new File(compressedPath);
        if (!inputFile.isFile() || !compressedFile.isFile()) {
            LoggerUtil.error("One or both input files not found.");
            System.exit(1);
            return;
        }

        long originalSize = inputFile.length();
        long compressedSize = compressedFile.length();
        double ratio = originalSize > 0 ? (1 - (double) compressedSize / originalSize) * 100 : 0;

        System.out.println("Original file:    " + inputPath + " (" + originalSize + " bytes)");
        System.out.println("Compressed file:  " + compressedPath + " (" + compressedSize + " bytes)");
        System.out.printf("Space saved:      %.2f%%%n", ratio);
    }

    private static boolean isHelp(String[] args) {
        return args.length > 1 && (args[1].equals("-h") || args[1].equals("--help"));
    }

    private static void printHelp() {
        System.out.println("HuffZip - A Huffman-coding based file compression CLI tool.");
        System.out.println();
        System.out.println("Usage:");
        System.out.println("  java -cp out huffzip.Main compress   <input_file> <output_file.huf>");
        System.out.println("  java -cp out huffzip.Main decompress <input_file.huf> <output_file>");
        System.out.println("  java -cp out huffzip.Main stats      <input_file> <compressed_file.huf>");
        System.out.println();
        System.out.println("Commands:");
        System.out.println("  compress     Compress a file");
        System.out.println("  decompress   Decompress a .huf file");
        System.out.println("  stats        Show compression statistics");
    }
}
