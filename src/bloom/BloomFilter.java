package bloom;

import bloom.hash.HashFunction;
import bloom.hash.HashFunctionFactory;

import java.math.BigInteger;

public class BloomFilter {

    private final BigInteger sizeInBits;
    private final byte[] filter;
    private final HashFunction[] hashFunctions;

    public static BloomFilter of(int elementCount, double epsilon) {
        var sizeInBites = calculateBitCount(elementCount, epsilon);
        var hashFunctionCount = calculateHashFunctionCount(epsilon);
        return new BloomFilter(sizeInBites, hashFunctionCount);
    }

    public void insert(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var bitIndex = hashCodeToBitIndex(hashAsBytes);
            var byteIndex = toFilterIndex(bitIndex);
            var mask = toByteMask(bitIndex);
            filter[byteIndex] |= mask;
        }
    }

    public boolean contains(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var bitIndex = hashCodeToBitIndex(hashAsBytes);
            var byteIndex = toFilterIndex(bitIndex);
            var mask = toByteMask(bitIndex);
            if ((filter[byteIndex] & mask) == 0) {
                return false;
            }
        }
        return true;
    }

    private BloomFilter(int sizeInBits, int hashFunctionCount) {
        this.sizeInBits = BigInteger.valueOf(sizeInBits);
        this.filter = new byte[calculateArraySize(sizeInBits)];
        this.hashFunctions = new HashFunction[hashFunctionCount];
        for (var i = 0; i < hashFunctionCount; i++) {
            this.hashFunctions[i] = HashFunctionFactory.createMurmur3(i);
        }
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
