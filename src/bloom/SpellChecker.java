package bloom;

import org.apache.commons.cli.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpellChecker {
    private static final String DEFAULT_BLOOM_FILTER_FILE = "words.bf";
    private static final double DEFAULT_EPSILON = 0.01;
    private static final int EXPECTED_INSERTIONS = 500_000;

    public static void main(String[] args) {
        try {
            Options options = createOptions();
            CommandLine commandLine = parseCommandLine(options, args);

            if (commandLine.hasOption("help")) {
                printHelp(options);
                return;
            }

            Path bloomFilterFile = getBloomFilterPath(commandLine);

            if (commandLine.hasOption("build")) {
                buildBloomFilter(commandLine, bloomFilterFile);
            }

            if (commandLine.hasOption("check")) {
                checkDocument(commandLine, bloomFilterFile);
            }
        } catch (ParseException e) {
            System.err.println("Error parsing command line: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
            System.exit(2);
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            System.exit(3);
        }
    }

    private static Options createOptions() {
        var options = new Options();
        
        var buildOption = Option.builder("build")
                .argName("file")
                .hasArg()
                .desc("dictionary file from which the bloom filter must be constructed, for example '/usr/share/dict/words'")
                .type(Path.class)
                .build();
                
        var epsilonOption = Option.builder("epsilon")
                .argName("number")
                .hasArg()
                .desc("false positive probability as a number between 0 and 1, default is %s".formatted(DEFAULT_EPSILON))
                .type(Double.class)
                .build();
                
        var bloomFilterFileOption = Option.builder("bloom-filter")
                .argName("file")
                .hasArg()
                .desc("bloom filter file, default is '%s'".formatted(DEFAULT_BLOOM_FILTER_FILE))
                .type(Path.class)
                .build();
                
        var checkOption = Option.builder("check")
                .argName("file")
                .hasArg()
                .desc("document to perform spell check on with a bloom filter")
                .type(Path.class)
                .build();
                
        var helpOption = Option.builder("help").build();
        
        options.addOption(buildOption);
        options.addOption(epsilonOption);
        options.addOption(bloomFilterFileOption);
        options.addOption(checkOption);
        options.addOption(helpOption);
        
        return options;
    }

    private static CommandLine parseCommandLine(Options options, String[] args) throws ParseException {
        var parser = new DefaultParser();
        return parser.parse(options, args);
    }

    private static void printHelp(Options options) {
        var formatter = new HelpFormatter();
        formatter.printHelp("spellchecker", "find spelling errors using a bloom filter", options, "", true);
    }

    private static Path getBloomFilterPath(CommandLine commandLine) throws ParseException {
        return commandLine.getParsedOptionValue("bloom-filter", Path.of(DEFAULT_BLOOM_FILTER_FILE));
    }

    private static void buildBloomFilter(CommandLine commandLine, Path bloomFilterFile) throws ParseException, IOException {
        Path dictionaryFile = commandLine.getParsedOptionValue("build");
        Double epsilon = commandLine.getParsedOptionValue("epsilon", DEFAULT_EPSILON);
        
        var bloomFilter = BloomFilter.of(EXPECTED_INSERTIONS, epsilon);
        
        // Load words from dictionary and insert into bloom filter
        try (Stream<String> lines = Files.lines(dictionaryFile)) {
            lines.map(line -> line.getBytes(StandardCharsets.UTF_8))
                 .forEach(bloomFilter::insert);
        }
        
        // Save bloom filter to file
        try (var out = Files.newOutputStream(bloomFilterFile);
             var buffered = new BufferedOutputStream(out)) {
            bloomFilter.serialize(buffered);
        }
        
        System.out.println("Bloom filter created successfully and saved to: " + bloomFilterFile);
    }

    private static void checkDocument(CommandLine commandLine, Path bloomFilterFile) throws ParseException, IOException {
        // Load bloom filter from file
        BloomFilter bloomFilter;
        try (var in = Files.newInputStream(bloomFilterFile);
             var buffered = new BufferedInputStream(in)) {
            bloomFilter = BloomFilter.of(buffered);
        }
        
        Path documentFile = commandLine.getParsedOptionValue("check");
        Set<String> incorrectWords;
        
        // Process document and find incorrect words
        try (Stream<String> lines = Files.lines(documentFile, StandardCharsets.UTF_8)) {
            incorrectWords = lines.flatMap(line -> Arrays.stream(line.split("\\s+")))
                                  .map(SpellChecker::processWord)
                                  .filter(word -> !word.isEmpty())
                                  .filter(word -> !bloomFilter.contains(word.getBytes(StandardCharsets.UTF_8)))
                                  .collect(Collectors.toSet());
        }
        
        System.out.printf("Found %d spelling errors in %s: %s%n", 
                           incorrectWords.size(), 
                           documentFile, 
                           String.join(", ", incorrectWords));
    }
    
    private static String processWord(String word) {
        return word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }
}
