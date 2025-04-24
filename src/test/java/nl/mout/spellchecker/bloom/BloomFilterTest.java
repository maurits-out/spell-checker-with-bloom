package nl.mout.spellchecker.bloom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class BloomFilterTest {
    private static final Random RANDOM = new Random();

    private BloomFilter bloomFilter;

    @BeforeEach
    public void setUp() {
        // Initialize with 1000 elements and 1% false positive rate
        bloomFilter = BloomFilter.of(1000, 0.01);
    }

    @Test
    public void testInsertAndContains() {
        var value = randomStr();
        var valueBytes = toBytes(value);

        assertThat(bloomFilter.contains(valueBytes))
                .as("Bloom filter should not contain element '%s' before insertion", value)
                .isFalse();

        bloomFilter.insert(valueBytes);

        assertThat(bloomFilter.contains(valueBytes))
                .as("Bloom filter should contain element '%s' after insertion", value)
                .isTrue();
    }

    @Test
    public void testMultipleInsertions() {
        String[] values = {randomStr(), randomStr(), randomStr(), randomStr()};
        for (var v : values) {
            bloomFilter.insert(toBytes(v));
        }

        for (var v : values) {
            assertThat(bloomFilter.contains(toBytes(v)))
                    .as("Bloom filter should contain element '%s'", v)
                    .isTrue();
        }
    }

    @Test
    public void testFalsePositiveRate() {
        // Insert 1000 known values
        for (var i = 0; i < 1000; i++) {
            bloomFilter.insert(toBytes(randomStr()));
        }

        // Check 1000 unknown values to estimate false positives
        var falsePositives = 0;
        for (var i = 0; i < 1000; i++) {
            if (bloomFilter.contains(toBytes(randomStr()))) {
                falsePositives++;
            }
        }

        var rate = falsePositives / 1000.0;
        assertThat(rate)
                .as("False positive rate should be small")
                .isLessThanOrEqualTo(0.02);
    }

    @Test
    void testSerialization() throws Exception {
        var values = IntStream.range(0, 1000).mapToObj(i -> randomStr()).collect(toSet());
        values.forEach(v -> bloomFilter.insert(toBytes(v)));

        var out = new ByteArrayOutputStream();
        bloomFilter.serialize(out);
        var buffer = out.toByteArray();

        var in = new ByteArrayInputStream(buffer);
        var newBloomFilter = BloomFilter.of(in);
        for (var v : values) {
            assertThat(newBloomFilter.contains(toBytes(v)))
                    .as("Deserialized Bloom filter should contain element '%s'", v)
                    .isTrue();
        }
    }

    @Test
    void throwExceptionWithIncorrectIdentifier() {
        var bytes = toBytes(randomStr());
        var in = new ByteArrayInputStream(bytes);
        assertThatExceptionOfType(BloomFilterException.class)
                .isThrownBy(() -> BloomFilter.of(in))
                .withMessage("Not a Bloom Filter");
    }

    @Test
    void throwExceptionWithIncorrectVersion() {
        var buffer = ByteBuffer.allocate(6);
        buffer.put("CCBF".getBytes(StandardCharsets.US_ASCII));
        buffer.putShort((short) 2);
        var in = new ByteArrayInputStream(buffer.array());
        assertThatExceptionOfType(BloomFilterException.class)
                .isThrownBy(() -> BloomFilter.of(in))
                .withMessage("Unsupported version: 2");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0, -0.1, 1, 1.5})
    void throwExceptionWithInvalidEpsilon(double invalidEpsilon) {
        assertThatExceptionOfType(BloomFilterException.class)
                .isThrownBy(() -> BloomFilter.of(1000, invalidEpsilon))
                .withMessage("epsilon must be greater than 0 and less than 1");
    }

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private String randomStr() {
        var sb = new StringBuilder(30);
        for (var i = 0; i < 30; i++) {
            sb.append((char) (32 + (RANDOM.nextInt(95))));
        }
        return sb.toString();
    }
}
