package nl.mout.spellchecker.bloom;

import nl.mout.spellchecker.bloom.hash.HashFunction;
import nl.mout.spellchecker.bloom.hash.HashFunctionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class BloomFilter {

    private static final byte[] BLOOM_FILTER_IDENTIFIER = "CCBF".getBytes(US_ASCII);
    private static final short BLOOM_FILTER_VERSION = 1;

    private final BigInteger sizeInBits;
    private final byte[] filter;
    private final List<HashFunction> hashFunctions;

    public static BloomFilter of(int elementCount, double epsilon) {
        if (epsilon <= 0 || epsilon >= 1) {
            throw new BloomFilterException("epsilon must be greater than 0 and less than 1");
        }
        var sizeInBits = calculateBitCount(elementCount, epsilon);
        var hashFunctionCount = calculateHashFunctionCount(epsilon);
        return new BloomFilter(sizeInBits, hashFunctionCount);
    }

    public static BloomFilter of(InputStream in) throws IOException {
        var buffer = ByteBuffer.wrap(in.readAllBytes()).order(ByteOrder.BIG_ENDIAN);
        var identifier = new byte[4];
        buffer.get(identifier);
        if (!Arrays.equals(BLOOM_FILTER_IDENTIFIER, identifier)) {
            throw new BloomFilterException("Not a Bloom Filter");
        }
        var version = buffer.getShort();
        if (version != BLOOM_FILTER_VERSION) {
            throw new BloomFilterException("Unsupported version: %d".formatted(version));
        }
        var hashFunctionCount = buffer.getShort();
        var sizeInBits = buffer.getInt();
        var bloomFilter = new BloomFilter(sizeInBits, hashFunctionCount);
        buffer.get(bloomFilter.filter);
        return bloomFilter;
    }

    public void insert(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var bitIndex = hashCodeToBitIndex(hashAsBytes);
            var index = toFilterIndex(bitIndex);
            var mask = toByteMask(bitIndex);
            filter[index] |= mask;
        }
    }

    public boolean contains(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var bitIndex = hashCodeToBitIndex(hashAsBytes);
            var index = toFilterIndex(bitIndex);
            var mask = toByteMask(bitIndex);
            if ((filter[index] & mask) == 0) {
                return false;
            }
        }
        return true;
    }

    public void serialize(OutputStream out) throws IOException {
        var buffer = ByteBuffer.allocate(filter.length + 12).order(ByteOrder.BIG_ENDIAN);
        buffer.put(BLOOM_FILTER_IDENTIFIER);
        buffer.putShort(BLOOM_FILTER_VERSION);
        buffer.putShort((short) hashFunctions.size());
        buffer.putInt(sizeInBits.intValueExact());
        buffer.put(filter, 0, filter.length);
        out.write(buffer.array());
    }

    private BloomFilter(int sizeInBits, int hashFunctionCount) {
        this.sizeInBits = BigInteger.valueOf(sizeInBits);
        this.filter = new byte[calculateArraySize(sizeInBits)];
        this.hashFunctions = IntStream.range(0, hashFunctionCount).mapToObj(HashFunctionFactory::createMurmur3).toList();
    }

    private static int calculateBitCount(int elementCount, double epsilon) {
        return (int) -((elementCount * Math.log(epsilon)) / (Math.pow(Math.log(2), 2)));
    }

    private static int calculateHashFunctionCount(double epsilon) {
        return (int) -(Math.log(epsilon) / Math.log(2));
    }

    private byte toByteMask(int bitIndex) {
        return (byte) (1 << (7 - (bitIndex & 0b111)));
    }

    private int toFilterIndex(int bitIndex) {
        return bitIndex >> 3;
    }

    private int hashCodeToBitIndex(byte[] hashAsBytes) {
        var hashAsInt = new BigInteger(1, hashAsBytes);
        return hashAsInt.mod(sizeInBits).intValueExact();
    }

    private int calculateArraySize(int sizeInBits) {
        var size = sizeInBits >> 3;
        if ((size & 0b111) != 0) {
            size++;
        }
        return size;
    }
}
