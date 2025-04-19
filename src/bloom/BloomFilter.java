package bloom;

import bloom.hash.HashFunction;
import bloom.hash.HashFunctionFactory;

import java.math.BigInteger;

public class BloomFilter {

    private final BigInteger sizeInBits;
    private final byte[] filter;
    private final HashFunction[] hashFunctions;

    public static BloomFilter of(int elementCount, double epsilon) {
        return new BloomFilter(calculateBitCount(elementCount, epsilon), calculateHashFunctionCount(epsilon));
    }

    private BloomFilter(int sizeInBits, int hashFunctionCount) {
        this.sizeInBits = BigInteger.valueOf(sizeInBits);
        filter = new byte[calculateArraySize(sizeInBits)];
        hashFunctions = new HashFunction[hashFunctionCount];
        for (var i = 0; i < hashFunctionCount; i++) {
            hashFunctions[i] = HashFunctionFactory.createMurmur3(i);
        }
    }

    private static int calculateBitCount(int elementCount, double epsilon) {
        return (int) -((elementCount * Math.log(epsilon)) / (Math.pow(Math.log(2), 2)));
    }

    private static int calculateHashFunctionCount(double epsilon) {
        return (int) -(Math.log(epsilon) / Math.log(2));
    }

    public void insert(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var hashAsInt = new BigInteger(1, hashAsBytes);
            var bitIndex = hashAsInt.mod(sizeInBits).intValueExact();
            var byteIndex = bitIndex >> 3;
            filter[byteIndex] |= (byte) (1 << (bitIndex & 0b111));
        }
    }

    public boolean contains(byte[] element) {
        for (var hashFunction : hashFunctions) {
            var hashAsBytes = hashFunction.hash(element);
            var hashAsInt = new BigInteger(1, hashAsBytes);
            var bitIndex = hashAsInt.mod(sizeInBits).intValueExact();
            var byteIndex = bitIndex >> 3;
            if ((filter[byteIndex] & (1 << (bitIndex & 0b111))) == 0) {
                return false;
            }
        }
        return true;
    }

    private int calculateArraySize(int sizeInBits) {
        var size = sizeInBits >> 3;
        if ((size & 0b111) != 0) {
            size++;
        }
        return size;
    }
}
