# Bloom Filter Spell Checker

A Java application that performs efficient spell checking using a Bloom filter data structure.

> **Project Inspiration**: This project was inspired by the Bloom Filter challenge from [Coding Challenges](https://codingchallenges.fyi/challenges/challenge-bloom/), a website featuring practical programming challenges to build your skills with real-world projects.

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

