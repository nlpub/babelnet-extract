package de.tudarmstadt.lt.babelnet.extract;

import de.tudarmstadt.lt.babelnet.extract.data.Cluster;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

public interface Resource {
    static <R> R readRecords(String filename, Function<CSVParser, R> f) throws IOException {
        try (final InputStream stream = new FileInputStream(filename);
             final Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             final CSVParser csv = CSVFormat.MYSQL.parse(reader)) {
            return f.apply(csv);
        }
    }

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

    static List<String> readSynsets(String filename) throws IOException {
        return readRecords(filename, csv -> {
            try {
                return csv.getRecords().stream().map(row -> row.get(0)).collect(toList());
            } catch (final IOException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    static void writeRecords(String filename, Consumer<CSVPrinter> f) throws IOException {
        try (final OutputStream stream = new FileOutputStream(filename);
             final Writer writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
             final CSVPrinter csv = CSVFormat.MYSQL.print(writer)) {
            f.accept(csv);
        }
    }

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
