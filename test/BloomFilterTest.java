import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class BloomFilterTest {
    private static final Random RANDOM = new Random();
    private BloomFilter bloomFilter;

    @BeforeEach
    public void setUp() {
        // Initialize with expected 1000 elements and 1% false positive rate
        bloomFilter = new BloomFilter(1000, 0.01);
    }

    @Test
    public void testInsertAndContains() {
        String value = randomStr();
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

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }

    private String randomStr() {
        var sb = new StringBuilder(30);
        for (var i = 0; i < 30; i++) {
            var ch = (char) ('!' + (RANDOM.nextInt(94)));
            sb.append(ch);
        }
        return sb.toString();
    }
}
