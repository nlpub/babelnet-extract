package de.tudarmstadt.lt.babelnet.extract;

import de.tudarmstadt.lt.babelnet.extract.actions.ClustersAction;
import de.tudarmstadt.lt.babelnet.extract.actions.NeighboursAction;
import de.tudarmstadt.lt.babelnet.extract.actions.SensesAction;
import de.tudarmstadt.lt.babelnet.extract.actions.SynsetsAction;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.data.BabelPOS;
import it.uniroma1.lcl.jlt.util.Language;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * BabelNet Extract is an application for extracting certain data from the BabelNet lexical ontology.
 *
 * @author Dmitry Ustalov
 */
abstract class Application {
    /**
     * This is the program entry point that parses the command line arguments and runs the specified actions.
     *
     * @param args the command line arguments.
     * @throws IOException when an I/O error has occurred.
     */
    public static void main(String[] args) throws IOException {
        final CommandLineParser parser = new DefaultParser();

        final Options options = new Options();
        options.addOption(Option.builder("action").argName("action").hasArg().required().build());
        options.addOption(Option.builder("clusters").argName("clusters").hasArg().build());
        options.addOption(Option.builder("synsets").argName("synsets").hasArg().build());
        options.addOption(Option.builder("words").argName("words").hasArg().build());
        options.addOption(Option.builder("neighbours").argName("neighbours").hasArg().build());
        options.addOption(Option.builder("senses").argName("senses").hasArg().build());
        options.addOption(Option.builder("depth").argName("depth").hasArg().build());
        options.addOption(Option.builder("language").argName("language").hasArg().build());
        options.addOption(Option.builder("pos").argName("pos").hasArg().build());

        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException ex) {
            System.err.println(ex.getMessage());
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar this.jar", options, true);
            System.exit(1);
        }

        final String action = Objects.requireNonNull(cmd.getOptionValue("action"), "-action needs to be specified");
        final BabelNet babelnet = BabelNet.getInstance();
        final Logger logger = Logger.getLogger("BabelNet");
        switch (action) {
            case "clusters": {
                final Language language = Resource.LANGUAGES.get(cmd.getOptionValue("language", "EN").toLowerCase());
                final BabelPOS pos = Resource.POS.get(cmd.getOptionValue("pos", "n").toLowerCase());
                final String clustersFilename = Objects.requireNonNull(cmd.getOptionValue("clusters"),
                        "-clusters needs to be specified");
                final String wordsFilename = cmd.getOptionValue("words", "synsets.txt");
                final String synsetsFilename = cmd.getOptionValue("synsets", "synsets.txt");
                new ClustersAction(babelnet, language, pos, clustersFilename, wordsFilename, synsetsFilename, logger).run();
                break;
            }
            case "neighbours": {
                final String synsetsFilename = Objects.requireNonNull(cmd.getOptionValue("synsets"),
                        "-synsets needs to be specified");
                final String neighboursFilename = cmd.getOptionValue("neighbours", "neighbours.txt");
                final int depth = Integer.valueOf(cmd.getOptionValue("depth", "1"));
                new NeighboursAction(babelnet, synsetsFilename, neighboursFilename, depth, logger).run();
                break;
            }
            case "senses": {
                final Language language = Resource.LANGUAGES.get(cmd.getOptionValue("language", "EN").toLowerCase());
                final String synsetsFilename = Objects.requireNonNull(cmd.getOptionValue("synsets"),
                        "-synsets needs to be specified");
                final String sensesFilename = cmd.getOptionValue("senses", "senses.txt");
                new SensesAction(babelnet, language, synsetsFilename, sensesFilename, logger).run();
                break;
            }
            case "synsets": {
                final Language language = Resource.LANGUAGES.get(cmd.getOptionValue("language", "EN").toLowerCase());
                final String synsetsFilename = cmd.getOptionValue("synsets", "synsets.txt");
                new SynsetsAction(babelnet, language, synsetsFilename, logger).run();
                break;
            }
            default:
                System.err.printf("Unknown action: \"%s\".\n", action);
                break;
        }
    }
}
