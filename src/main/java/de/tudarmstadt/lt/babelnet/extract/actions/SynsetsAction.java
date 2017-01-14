package de.tudarmstadt.lt.babelnet.extract.actions;

import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tudarmstadt.lt.babelnet.extract.Resource.writeRecords;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * The synsets action extracts the list of synsets for the given language.
 *
 * @author Dmitry Ustalov
 */
public class SynsetsAction {
    private final BabelNet babelnet;
    private final Language language;
    private final String synsetsFilename;
    private final Logger logger;

    /**
     * Initialize the action.
     *
     * @param babelnet        the BabelNet instance.
     * @param language        the language.
     * @param synsetsFilename the synsets output file.
     * @param logger          the logger instance.
     */
    public SynsetsAction(BabelNet babelnet, Language language, String synsetsFilename, Logger logger) {
        this.babelnet = babelnet;
        this.language = language;
        this.synsetsFilename = synsetsFilename;
        this.logger = logger;
        logger.log(Level.INFO, "Writing synsets to \"{0}\"", synsetsFilename);
    }

    /**
     * Process the data and write the outputs.
     *
     * @throws IOException when an I/O error has occurred.
     */
    public void run() throws IOException {
        writeRecords(synsetsFilename, csv -> {
            babelnet.getSynsetIterator().forEachRemaining(synset -> {
                try {
                    final List<BabelSense> senses = synset.getSenses(language);
                    if (senses.isEmpty()) return;

                    final String synsetID = synset.getId().toString();

                    final Set<String> lemmas = senses.stream().map(BabelSense::getSimpleLemma).collect(toSet());
                    csv.printRecord(synsetID, lemmas.size(), lemmas.stream().collect(joining(", ")));

                    logger.log(Level.INFO, "Extracted {0}", synsetID);
                } catch (final IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        });

        logger.log(Level.INFO, "Done");
    }
}
