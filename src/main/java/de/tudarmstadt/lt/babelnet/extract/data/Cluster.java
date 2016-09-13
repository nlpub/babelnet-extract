package de.tudarmstadt.lt.babelnet.extract.data;

import org.inferred.freebuilder.FreeBuilder;

import java.util.List;

import static java.util.stream.Collectors.toList;

@FreeBuilder
public interface Cluster {
    Integer getId();

    List<String> getSenses();

    List<String> getLemmas();

    class Builder extends Cluster_Builder {
        public Cluster build() {
            addAllLemmas(getSenses().stream().map(sense -> sense.split("#")[0].toLowerCase()).collect(toList()));
            return super.build();
        }
    }
}
