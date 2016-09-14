package de.tudarmstadt.lt.babelnet.extract.actions;

import de.tudarmstadt.lt.babelnet.extract.data.Cluster;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tudarmstadt.lt.babelnet.extract.Resource.readClusters;
import static de.tudarmstadt.lt.babelnet.extract.Resource.writeRecords;
import static java.util.Collections.synchronizedMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * The synsets action extracts the list of synsets per given clusters and the list of the synsets containing
 * the words in these clusters.
 *
 * @author Dmitry Ustalov
 */
public class SynsetsAction {
    private final BabelNet babelnet;
    private final String clustersFilename, wordsFilename, synsetsFilename;
    private final Logger logger;

    /**
     * Initialize the action.
     *
     * @param babelnet the BabelNet instance.
     * @param clustersFilename the clusters input file.
     * @param wordsFilename the words output file.
     * @param synsetsFilename the synsets output file.
     * @param logger the logger instance.
     */
    public SynsetsAction(BabelNet babelnet, String clustersFilename, String wordsFilename, String synsetsFilename, Logger logger) {
        this.babelnet = babelnet;
        this.clustersFilename = clustersFilename;
        this.wordsFilename = wordsFilename;
        this.synsetsFilename = synsetsFilename;
        this.logger = logger;
        logger.log(Level.INFO, "Reading clusters from \"{0}\"", clustersFilename);
        logger.log(Level.INFO, "Writing words to \"{0}\"", wordsFilename);
        logger.log(Level.INFO, "Writing synsets to \"{0}\"", synsetsFilename);
    }

    /**
     * Process the data and write the outputs.
     *
     * @throws IOException when an I/O error has occurred.
     */
    public void run() throws IOException {
        final Map<Integer, Cluster> allClusters = synchronizedMap(readClusters(clustersFilename));
        final Set<String> allSynsets = new ConcurrentSkipListSet<>();

        writeRecords(wordsFilename, csv ->
                allClusters.values().parallelStream().forEach(cluster -> {
                    try {
                        logger.log(Level.INFO, "Extracting {0}", cluster.getId().toString());
                        final Map<String, Collection<String>> synsets = new HashMap<>();
                        for (final String lemma : cluster.getLemmas()) {
                            final Collection<String> lemmaSynsets = babelnet.
                                    getSynsets(lemma, Language.EN, BabelPOS.NOUN).stream().
                                    map(BabelSynset::getId).map(BabelSynsetID::toString).
                                    collect(toSet());
                            synsets.put(lemma, lemmaSynsets);
                            allSynsets.addAll(lemmaSynsets);
                        }
                        synchronized (csv) {
                            for (final Map.Entry<String, Collection<String>> entry : synsets.entrySet()) {
                                csv.printRecord(
                                        cluster.getId().toString(),
                                        entry.getKey(),
                                        entry.getValue().stream().collect(joining(","))
                                );
                            }
                        }
                        logger.log(Level.INFO, "Extracted {0}", cluster.getId().toString());
                    } catch (final IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );

        writeRecords(synsetsFilename, allSynsets);
        logger.log(Level.INFO, "Done");
    }
}
