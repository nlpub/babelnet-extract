package de.tudarmstadt.lt.babelnet.extract.data;

import org.inferred.freebuilder.FreeBuilder;

import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * The representation of the cluster in Chinese Whispers.
 *
 * @author Dmitry Ustalov
 */
@FreeBuilder
public interface Cluster {
    /**
     * Get the cluster ID.
     *
     * @return the cluster ID.
     */
    Integer getId();

    /**
     * Get the cluster senses provided with the sense lables.
     *
     * @return the senses.
     */
    List<String> getSenses();

    /**
     * Get the cluster lemmas, i.e., the cluster senses with the labels removed.
     *
     * @return the lemmas.
     */
    List<String> getLemmas();

    /**
     * A builder for the Cluster instances.
     */
    class Builder extends Cluster_Builder {
        /**
         * Build a Cluster instance.
         *
         * @return the new cluster instance.
         */
        public Cluster build() {
            addAllLemmas(getSenses().stream().map(sense -> sense.split("#")[0].toLowerCase()).collect(toList()));
            return super.build();
        }
    }
}
