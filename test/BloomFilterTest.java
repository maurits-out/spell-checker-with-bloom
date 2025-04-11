import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class BloomFilterTest {

    private BloomFilter bloomFilter;

    @BeforeEach
    public void setUp() {
        // Initialize with expected 1000 elements and 1% false positive rate
        bloomFilter = new BloomFilter(1000, 0.01);
    }

    @Test
    public void testInsertAndContains() {
        String value = "test-value";
        var valueBytes = toBytes(value);

        assertThat(bloomFilter.contains(valueBytes))
                .as("Bloom filter should not contain the element before insertion")
                .isFalse();

        bloomFilter.insert(valueBytes);

        assertThat(bloomFilter.contains(valueBytes))
                .as("Bloom filter should contain the element after insertion")
                .isTrue();
    }

    @Test
    public void testMultipleInsertions() {
        String[] values = {"alpha", "beta", "gamma", "delta"};
        Arrays.stream(values)
                .map(this::toBytes)
                .forEach(bytes -> bloomFilter.insert(bytes));

        Arrays.stream(values).forEach(v ->
                assertThat(bloomFilter.contains(toBytes(v)))
                        .as("Bloom filter should contain inserted element: %s", v)
                        .isTrue());
    }

    @Test
    public void testFalsePositiveRate() {
        // Insert 1000 known values
        IntStream.range(0, 1000).forEach(i -> bloomFilter.insert(toBytes("known-" + i)));

        // Check 1000 unknown values to estimate false positives
        var falsePositives = IntStream.range(0, 1000)
                .filter(i -> bloomFilter.contains(toBytes("unknown-" + i)))
                .count();

        double rate = falsePositives / 1000.0;
        System.out.printf("Estimated false positive rate: %.4f%n", rate);

        assertThat(rate)
                .as("False positive rate should not exceed 5%%")
                .isLessThanOrEqualTo(0.05);
    }

    private byte[] toBytes(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
