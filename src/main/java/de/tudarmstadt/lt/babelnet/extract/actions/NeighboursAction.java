package de.tudarmstadt.lt.babelnet.extract.actions;

import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelPointer;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.tudarmstadt.lt.babelnet.extract.Resource.readSynsets;
import static de.tudarmstadt.lt.babelnet.extract.Resource.writeRecords;
import static java.util.Collections.synchronizedList;
import static java.util.stream.Collectors.joining;

public class NeighboursAction {
    private final BabelNet babelnet;
    private final String synsetsFilename, neighboursFilename;
    private final int depth;
    private final Logger logger;

    public NeighboursAction(BabelNet babelnet, String synsetsFilename, String neighboursFilename, int depth, Logger logger) {
        this.babelnet = babelnet;
        this.synsetsFilename = synsetsFilename;
        this.neighboursFilename = neighboursFilename;
        this.depth = depth;
        this.logger = logger;
        logger.log(Level.INFO, "Reading synsets from \"{0}\"", synsetsFilename);
        logger.log(Level.INFO, "Writing neighbours to \"{0}\"", neighboursFilename);
        logger.log(Level.INFO, "Extracting in {0} steps", Integer.toString(depth));
    }

    public void run() throws IOException {
        final List<String> allSynsets = synchronizedList(readSynsets(synsetsFilename));

        writeRecords(neighboursFilename, neighboursCSV ->
                allSynsets.parallelStream().forEach(synsetID -> {
                    logger.log(Level.INFO, "Processing {0}", synsetID);
                    try {
                        final BabelSynset synset = babelnet.getSynset(new BabelSynsetID(synsetID));
                        final Map<String, Integer> neighbours = walk(synset);
                        if (!neighbours.isEmpty()) {
                            synchronized (neighboursCSV) {
                                neighboursCSV.printRecord(
                                        synsetID,
                                        neighbours.entrySet().stream().
                                                map(entry -> entry.getKey() + ':' + entry.getValue()).
                                                collect(joining(","))
                                );
                            }
                        }
                        logger.log(Level.INFO, "Processed {0}, found {1} neighbour(s)",
                                new String[]{synsetID, Integer.toString(neighbours.size())});
                    } catch (final InvalidBabelSynsetIDException | IOException ex) {
                        throw new RuntimeException(ex);
                    }
                })
        );
    }

    private Map<String, Integer> walk(BabelSynset source) throws IOException {
        final Map<String, Integer> neighbours = new HashMap<>();
        neighbours.put(source.getId().toString(), 0);

        final Queue<BabelSynset> queue = new LinkedList<>();
        queue.add(source);

        while (!queue.isEmpty()) {
            final BabelSynset synset = queue.remove();
            final List<BabelSynsetIDRelation> edges = synset.getEdges(BabelPointer.ANY_HYPERNYM, BabelPointer.ANY_HYPONYM);
            for (final BabelSynsetIDRelation edge : edges) {
                int step = neighbours.get(synset.getId().toString());
                if (!neighbours.containsKey(edge.getTarget()) && Math.abs(step) < depth) {
                    int level = (step == 0) ?
                            (edge.getPointer().isHypernym() ? +1 : -1) :
                            Integer.signum(step) * (Math.abs(step) + 1);
                    neighbours.put(edge.getTarget(), level);
                    queue.add(edge.getBabelSynsetIDTarget().toBabelSynset());
                }
            }
        }

        neighbours.remove(source.getId().toString());
        return neighbours;
    }
}
