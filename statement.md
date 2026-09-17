# Problem Statement — HuffZip

## Problem Statement

Storage and bandwidth are limited resources. Files — especially text
logs, source code, and documents with repetitive or skewed character
distributions — often contain redundancy that generic byte-per-character
storage does not exploit. There is a need for a simple, transparent,
lossless compression tool that a user can run from the command line to
reduce file size for storage or transfer, without relying on external
libraries or opaque third-party compression formats.

## Scope of the Project

- Compress and decompress arbitrary files (text or binary) losslessly
  using the Huffman coding algorithm.
- Provide a command-line interface with three operations: `compress`,
  `decompress`, and `stats`.
- Implement the algorithm's core data structures (binary tree,
  min-heap, hash maps) from scratch using only the Java Standard
  Library (`java.util`, `java.io`, `java.util.logging`), rather than
  calling an existing compression library such as `java.util.zip`.
- Handle edge cases explicitly: empty files, files with a single
  repeated byte value, and arbitrary binary (non-text) data.
- Out of scope: directory/batch compression, adaptive/streaming
  compression of data too large to fit in memory, and a graphical
  user interface (the project is intentionally CLI-only, per the
  project's execution requirements).

## Target Users

- Students and instructors evaluating DSA implementations of classic
  compression algorithms in Java.
- Developers who want a transparent, inspectable compression format
  (as opposed to a black-box library) for educational or lightweight
  use cases.
- Anyone needing to reduce the size of text-heavy files (logs,
  source code, configuration files) for storage or transfer without
  installing additional software or build tools.

## High-Level Features

1. **Compress** any input file into a custom `.huf` binary format
   that stores just enough information (a frequency table) to
   reconstruct the exact Huffman tree later.
2. **Decompress** a `.huf` file back into a byte-for-byte identical
   copy of the original file.
3. **Report compression statistics** — original size, compressed
   size, and percentage of space saved — so the effectiveness of the
   algorithm on a given file is immediately visible.
4. **Fail safely and informatively** on invalid input (missing files,
   empty files, corrupted `.huf` headers) rather than crashing with
   an unhandled exception.
