package bloom.hash;

public class HashFunctionFactory {

    public static HashFunction createMurmur3(int seed) {
        return new Murmur3HashFunction(seed);
    }
}
