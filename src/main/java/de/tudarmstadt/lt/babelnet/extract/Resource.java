package de.tudarmstadt.lt.babelnet.extract;

import de.tudarmstadt.lt.babelnet.extract.data.Cluster;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.jlt.util.Language;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

/**
 * An interface containing the input/output routines used by other classes.
 *
 * @author Dmitry Ustalov
 */
public interface Resource {
    /**
     * The map of available languages in BabelNet.
     */
    Map<String, Language> LANGUAGES = Stream.of(Language.values()).collect(Collectors.toMap(l -> l.getName().toLowerCase(), Function.identity()));

    /**
     * The map of available parts of speech in BabelNet.
     */
    Map<String, BabelPOS> POS = Stream.of(BabelPOS.values()).collect(Collectors.toMap(p -> String.valueOf(p.getTag()), Function.identity()));

    /**
     * Open the specified file for reading and pass the CSV parser to the given function once.
     *
     * @param filename the file to read.
     * @param f        the function to pass the parser.
     * @param <R>      the return type of the given function.
     * @return the return value of the given function.
     * @throws IOException when an I/O error has occurred.
     */
    static <R> R readRecords(String filename, Function<CSVParser, R> f) throws IOException {
        try (final InputStream stream = new FileInputStream(filename);
             final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final CSVParser csv = CSVFormat.MYSQL.parse(reader)) {
            return f.apply(csv);
        }
    }

    /**
     * Open the Chinese Whispers clusters file and parse the records.
     *
     * @param filename the file to read.
     * @return the mapping between a set of cluster IDs and their representations.
     * @throws IOException when an I/O error has occurred.
     */
    static Map<Integer, Cluster> readClusters(String filename) throws IOException {
        return readRecords(filename, csv -> {
            final Map<Integer, Cluster> map = new HashMap<>();
            for (final CSVRecord row : csv) {
                final Integer id = Integer.parseInt(row.get(0));
                final List<String> senses = Arrays.asList(row.get(2).substring(0, row.get(2).length() - 2).split(", "));
                map.put(id, new Cluster.Builder().setId(id).addAllSenses(senses).build());
            }
            return map;
        });
    }

    /**
     * Parse the synset list.
     *
     * @param filename the file to read.
     * @return the list of synset IDs.
     * @throws IOException when an I/O error has occurred.
     */
    static List<String> readSynsets(String filename) throws IOException {
        return readRecords(filename, csv -> {
            try {
                return csv.getRecords().stream().map(row -> row.get(0)).collect(toList());
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Open the specified class for writing and pass the CSV printer to the given consumer once.
     *
     * @param filename the file to write.
     * @param f        the consumer to pass the printer.
     * @throws IOException when an I/O error has occurred.
     */
    static void writeRecords(String filename, Consumer<CSVPrinter> f) throws IOException {
        try (final OutputStream stream = new FileOutputStream(filename);
             final Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
             final CSVPrinter csv = CSVFormat.MYSQL.print(writer)) {
            f.accept(csv);
        }
    }

    /**
     * Open the specified class for writing the given string collection.
     *
     * @param filename the file to write.
     * @param records  the collection of strings to write to the file.
     * @throws IOException when an I/O error has occurred.
     */
    static void writeRecords(String filename, Collection<String> records) throws IOException {
        writeRecords(filename, csv -> {
            try {
                csv.printRecords(records);
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
}
