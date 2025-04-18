package bloom.hash;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

record Murmur3HashFunction(int seed) implements HashFunction {

    private static final long C1 = 0x87c37b91114253d5L;
    private static final long C2 = 0x4cf5ad432745937fL;

    @Override
    public byte[] hash(byte[] key) {
        final int length = key.length;
        final int blockCount = length >> 4;

        long h1 = seed;
        long h2 = seed;

        ByteBuffer buffer = ByteBuffer.wrap(key).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < blockCount; i++) {
            long k1 = buffer.getLong();
            long k2 = buffer.getLong();

            k1 *= C1;
            k1 = Long.rotateLeft(k1, 31);
            k1 *= C2;
            h1 ^= k1;

            h1 = Long.rotateLeft(h1, 27);
            h1 += h2;
            h1 = (h1 * 5) + 0x52dce729;

            k2 *= C2;
            k2 = Long.rotateLeft(k2, 33);
            k2 *= C1;
            h2 ^= k2;

            h2 = Long.rotateLeft(h2, 31);
            h2 += h1;
            h2 = (h2 * 5) + 0x38495ab5;
        }

        long k1 = 0;
        long k2 = 0;

        int tailStart = blockCount * 16;
        int remaining = length & 15;
        switch (remaining) {
            case 15:
                k2 ^= (key[tailStart + 14] & 0xffL) << 48;
            case 14:
                k2 ^= (key[tailStart + 13] & 0xffL) << 40;
            case 13:
                k2 ^= (key[tailStart + 12] & 0xffL) << 32;
            case 12:
                k2 ^= (key[tailStart + 11] & 0xffL) << 24;
            case 11:
                k2 ^= (key[tailStart + 10] & 0xffL) << 16;
            case 10:
                k2 ^= (key[tailStart + 9] & 0xffL) << 8;
            case 9:
                k2 ^= (key[tailStart + 8] & 0xffL);
                k2 *= C2;
                k2 = Long.rotateLeft(k2, 33);
                k2 *= C1;
                h2 ^= k2;

            case 8:
                k1 ^= (key[tailStart + 7] & 0xffL) << 56;
            case 7:
                k1 ^= (key[tailStart + 6] & 0xffL) << 48;
            case 6:
                k1 ^= (key[tailStart + 5] & 0xffL) << 40;
            case 5:
                k1 ^= (key[tailStart + 4] & 0xffL) << 32;
            case 4:
                k1 ^= (key[tailStart + 3] & 0xffL) << 24;
            case 3:
                k1 ^= (key[tailStart + 2] & 0xffL) << 16;
            case 2:
                k1 ^= (key[tailStart + 1] & 0xffL) << 8;
            case 1:
                k1 ^= (key[tailStart] & 0xffL);
                k1 *= C1;
                k1 = Long.rotateLeft(k1, 31);
                k1 *= C2;
                h1 ^= k1;
        }

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        ByteBuffer outBuffer = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        outBuffer.putLong(h1);
        outBuffer.putLong(h2);
        return outBuffer.array();
    }

    private static long fmix64(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;
        return k;
    }
}
