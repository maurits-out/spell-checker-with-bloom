# Bloom Filter Spell Checker

A Java application that performs efficient spell checking using a Bloom filter data structure.

## Overview

This project provides an implementation of a spell checker that uses a Bloom filter to efficiently check if words are potentially misspelled. A Bloom filter is a space-efficient probabilistic data structure that is used to test whether an element is a member of a set. It may produce false positives (indicating a word is correctly spelled when it's not), but it will never produce false negatives (all truly misspelled words will be found).

Key components of the project:

- `BloomFilter`: A probabilistic data structure that can determine if an element is possibly in a set or definitely not in a set
- `SpellChecker`: A command-line tool that builds Bloom filters from dictionaries and checks documents for spelling errors
- `Murmur3HashFunction`: The hash function implementation used by the Bloom filter

## How It Works

1. **Building the Bloom Filter**:
   - A dictionary file (containing correctly spelled words) is read
   - Each word is inserted into a Bloom filter, which uses multiple hash functions to set bits in a bit array
   - The filter is serialized to a file for later use

2. **Spell Checking**:
   - A document is read and split into words
   - Each word is checked against the Bloom filter
   - Words not found in the filter are reported as potential spelling errors

3. **Advantages of Bloom Filters for Spell Checking**:
   - Memory efficient: Requires significantly less memory than storing the entire dictionary
   - Fast lookups: O(k) time complexity where k is the number of hash functions
   - No false negatives: If a word is actually in the dictionary, it will always be recognized

## Usage

### Command Line Options

The Bloom Filter Spell Checker supports the following command-line options:

| Option | Description | Required | Default |
|--------|-------------|----------|---------|
| `-build <file>` | Specifies the dictionary file to use for constructing the Bloom filter. This should be a text file with one word per line. Common locations include `/usr/share/dict/words` on Unix-like systems. | No | None |
| `-check <file>` | Specifies the document to check for spelling errors. The document will be tokenized, and each word will be checked against the Bloom filter. | No | None |
| `-bloom-filter <file>` | Specifies the path to the Bloom filter file. This can be a file to save to when using `-build`, or a file to read from when using `-check`. | No | `words.bf` |
| `-epsilon <number>` | Specifies the desired false positive probability as a decimal between 0 and 1. Lower values provide better accuracy but require more memory. | No | 0.01 (1%) |
| `-help` | Displays the help message with information about the available options. | No | None |

#### Option Details:

- **-build**: This option is used to construct a new Bloom filter from a dictionary file. The dictionary file should contain correctly spelled words, one per line. The resulting Bloom filter will be saved to the file specified by `-bloom-filter` (or `words.bf` by default).

- **-check**: This option is used to check a document for spelling errors using a previously built Bloom filter. Words not found in the Bloom filter will be reported as potential spelling errors. The program will load the Bloom filter from the file specified by `-bloom-filter` (or `words.bf` by default).

- **-bloom-filter**: This option specifies the path to the Bloom filter file. When used with `-build`, it determines where the new Bloom filter will be saved. When used with `-check`, it determines which Bloom filter file to load.

- **-epsilon**: This option controls the false positive rate of the Bloom filter. A false positive means that a misspelled word might incorrectly be identified as correctly spelled. Lower values of epsilon reduce the false positive rate but increase the size of the Bloom filter. The value must be between 0 and 1, with 0.01 (1%) being the default.

- **-help**: This option displays a help message showing all available command-line options and their descriptions.
