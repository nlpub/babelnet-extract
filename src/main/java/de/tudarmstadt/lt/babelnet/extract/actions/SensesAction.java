package de.tudarmstadt.lt.babelnet.extract.actions;

import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tudarmstadt.lt.babelnet.extract.Resource.readSynsets;
import static de.tudarmstadt.lt.babelnet.extract.Resource.writeRecords;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;

public class SensesAction {
    private final BabelNet babelnet;
    private final String synsetsFilename, sensesFilename;
    private final Logger logger;

    public SensesAction(BabelNet babelnet, String synsetsFilename, String sensesFilename, Logger logger) {
        this.babelnet = babelnet;
        this.synsetsFilename = synsetsFilename;
        this.sensesFilename = sensesFilename;
        this.logger = logger;
        logger.log(Level.INFO, "Reading synsets from \"{0}\"", synsetsFilename);
        logger.log(Level.INFO, "Writing senses to \"{0}\"", sensesFilename);
    }

    public void run() throws IOException {
        final List<String> allSynsets = synchronizedList(readSynsets(synsetsFilename));

        writeRecords(sensesFilename, csv ->
                allSynsets.parallelStream().forEach(synsetID -> {
                    try {
                        final BabelSynset synset = babelnet.getSynset(new BabelSynsetID(synsetID));
                        final Map<String, Integer> senses = synset.getSenses(Language.EN).stream().
                                collect(toMap(sense -> sense.getSimpleLemma().replaceAll("_", " "),
                                        BabelSense::getFrequency,
                                        (v1, v2) -> v1,
                                        () -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)));
                        if (!senses.isEmpty()) {
                            synchronized (csv) {
                                csv.printRecord(
                                        synsetID,
                                        senses.entrySet().stream().map(entry -> entry.getKey() + ':' + entry.getValue()).
                                                collect(joining(","))
                                );
                            }
                        }
                        logger.log(Level.INFO, "Extracted {0}", synsetID);
                    } catch (final InvalidBabelSynsetIDException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );

        logger.log(Level.INFO, "Done");
    }
}
