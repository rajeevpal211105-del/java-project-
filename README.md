# HuffZip — Huffman Coding File Compression Tool (Java)

A command-line file compression and decompression tool built from
scratch in **Java**, implementing the **Huffman coding algorithm** —
a classic greedy algorithm from Data Structures & Algorithms that
achieves lossless compression using binary trees and min-heaps.

## Overview

HuffZip takes any file (text or binary), builds a frequency table of
byte values, constructs an optimal binary prefix-code tree (the
**Huffman tree**) using a min-heap (`PriorityQueue`), and re-encodes
the file using variable-length binary codes — shorter codes for
frequent bytes, longer codes for rare ones. The result is a smaller
`.huf` file that can be decompressed back into an **exact,
byte-for-byte copy** of the original.

## Features (Functional Modules)

1. **Compression Module** (`huffman/HuffmanEncoder.java`,
   `huffman/TreeBuilder.java`)
   Builds the frequency table, constructs the Huffman tree via a
   min-heap based greedy algorithm, generates prefix-free binary
   codes via tree traversal, and encodes the file.

2. **Decompression Module** (`huffman/HuffmanDecoder.java`)
   Reconstructs the exact same Huffman tree from the stored frequency
   table and walks it bit-by-bit to losslessly restore the original
   data.

3. **File I/O & Statistics Module** (`huffman/FileHandler.java`,
   `Main.java`)
   Reads/writes the custom `.huf` binary format (header + frequency
   table + packed bitstream), and reports compression ratio /
   space-saved statistics via the CLI.

## Data Structures & Algorithms Used

| Concept | Where it's used |
|---|---|
| Binary Tree | Huffman tree structure (`Node`) |
| Min-Heap (Priority Queue) | Greedy tree construction (`java.util.PriorityQueue`) |
| Hash Map | Frequency table & code map (`LinkedHashMap` / `HashMap`) |
| Greedy Algorithm | Repeatedly merging lowest-frequency nodes |
| Tree Traversal (DFS) | Generating prefix codes from the tree (recursion) |
| Bit Manipulation | Packing/unpacking bit strings into real bytes |

## Non-Functional Requirements

- **Performance:** Tree construction runs in O(k log k) where k ≤ 256
  (distinct byte values), independent of file size.
- **Reliability:** Empty files, missing files, and corrupted `.huf`
  headers are all explicitly checked and throw clear exceptions
  instead of crashing with an unhandled stack trace.
- **Usability:** A simple three-command CLI (`compress`,
  `decompress`, `stats`) with built-in `-h` / `--help` text.
- **Maintainability:** Code is split into small, single-responsibility
  classes (tree building, encoding, decoding, file I/O) with Javadoc
  comments throughout.
- **Logging/Monitoring:** All major operations (tree built, file
  written, decode complete, errors) are logged via `java.util.logging`
  through a central `LoggerUtil` class.
- **Scalability:** The frequency table only ever holds at most 256
  entries regardless of input file size, so memory use for the tree
  itself stays constant even on large files.

## Project Structure

```
huffzip-java/
├── src/
│   └── huffzip/
│       ├── Main.java                  # CLI entry point
│       ├── huffman/
│       │   ├── Node.java              # Huffman tree node
│       │   ├── TreeBuilder.java       # Frequency table + tree construction (min-heap)
│       │   ├── HuffmanEncoder.java    # Code generation + encoding
│       │   ├── HuffmanDecoder.java    # Tree reconstruction + decoding
│       │   └── FileHandler.java       # .huf binary file format read/write
│       └── utils/
│           └── LoggerUtil.java        # Central logging configuration
├── tests/
│   └── huffzip/
│       └── HuffmanTest.java           # Unit tests (9 tests, dependency-free)
├── data/
│   └── sample.txt                     # Sample file for demo/testing
├── README.md
├── statement.md
└── .gitignore
```

## Requirements

- **JDK 17 or higher** (developed and tested on JDK 21)
- No external dependencies — no Maven, no Gradle, no JUnit. Uses only
  the Java Standard Library (`java.util`, `java.io`,
  `java.util.logging`). Compiles and runs with just `javac` / `java`.

## Setup & Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/<your-username>/<your-repo-name>.git
   cd <your-repo-name>
   ```

2. **Verify you have a JDK installed**
   ```bash
   javac -version
   ```
   If this fails, install a JDK (e.g. `sudo apt install openjdk-21-jdk`
   on Ubuntu/Debian, or download from [Adoptium](https://adoptium.net/)).

3. **No dependency installation needed** — the project uses only the
   Java Standard Library, so there is no `pom.xml` / `build.gradle`
   to resolve.

## How to Build

All commands are run from the project root directory.

```bash
mkdir -p out
javac -d out $(find src tests -name "*.java")
```

This compiles both the main source (`src/`) and the tests (`tests/`)
into the `out/` directory, with the `huffzip` package structure
preserved.

## How to Run

### Compress a file
```bash
java -cp out huffzip.Main compress data/sample.txt data/sample.huf
```

### Decompress a file
```bash
java -cp out huffzip.Main decompress data/sample.huf data/sample_restored.txt
```

### View compression statistics
```bash
java -cp out huffzip.Main stats data/sample.txt data/sample.huf
```

### Verify a round trip (Linux/macOS)
```bash
diff data/sample.txt data/sample_restored.txt && echo "Files are identical!"
```

### Get help
```bash
java -cp out huffzip.Main -h
```

## How to Test

Unit tests cover frequency counting, prefix-code correctness, full
encode/decode round trips (including edge cases like empty files,
single-repeated-character files, and binary data), and the bit-packing
helpers. The test runner is a small dependency-free harness (no JUnit
required), so it builds with the same single `javac` command above.

```bash
java -cp out huffzip.HuffmanTest
```

Expected output: all 9 tests print `[PASS]`, ending with
`9 passed, 0 failed`.

## Example Output

```
$ java -cp out huffzip.Main compress data/sample.txt data/sample.huf
[INFO] huffzip: Generated 50 unique Huffman codes.
[INFO] huffzip: Wrote compressed file: data/sample.huf
[INFO] huffzip: Compression complete.
[INFO] huffzip:   Original size:   1043 bytes
[INFO] huffzip:   Compressed size: 852 bytes
[INFO] huffzip:   Space saved:     18.31%
```

## Future Enhancements

- Support compressing entire directories (batch mode)
- Adaptive/canonical Huffman coding to shrink the stored header further
- Progress bar for very large files
- Compare against alternative algorithms (e.g. Shannon-Fano) for the report's evaluation section

## License

This project was built as an academic submission for a Data
Structures & Algorithms course evaluation.
