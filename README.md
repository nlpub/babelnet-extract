BabelNet Extract
================

This program extracts certain data from the BabelNet lexical ontology. There are three modes available for synset, sense, and neighbourhood extraction, correspondingly. The data processing routines are implemented using multithreading, so they should scale well as long as the underlying storage allows.

[![Build Status][travis_ci_badge]][travis_ci_link]

[travis_ci_badge]: https://travis-ci.org/tudarmstadt-lt/babelnet-extract.svg
[travis_ci_link]: https://travis-ci.org/tudarmstadt-lt/babelnet-extract

Usage
-----

For running this program Java 8 and Maven 3 are required among the working BabelNet [Java API](http://babelnet.org/download) setup. The BabelNet API configuration files should be located in the working directory from which the program is run.

### Synset Extraction

Given the set of word sense clusters, this action writes two files: `words.txt` with the list of synsets per clusters, and `synsets.txt` with the list of the synsets containing the input words. The paths of both output files can be specified using the `-words` and `-synsets` options, correspondingly.

```bash
java -jar target/babelnet-extract.jar -action synsets -clusters "clusters.txt" -words "words.txt" -synsets "synsets.txt"
```

The `clusters.txt` input file should be formatted according to the Chinese Whispers [program](https://github.com/tudarmstadt-lt/chinese-whispers) tab separated output format `cluster<TAB>size<TAB>senses` as follows. Note that the sense labels like `#1`, `#2` and `#3` are ignored by the parser.

```
0	2	word#1, word#2
1	1	word#3
```

### Sense Extraction

Given the set of synsets, extract the corresponding sense lemmas and their frequencies, and write the file `senses.txt`, the path of which can be specified using the `-senses` option.

```bash
java -jar target/babelnet-extract.jar -action senses -synsets "synsets.txt" -senses "senses.txt"
```

The `synsets.txt` input file should be produced by the synset extraction action containing a list of BabelNet synset identifiers.

### Neighbourhood Extraction

Given the set of synsets, extract the n-level ego network for each of them and write the tab separated file `neighbours.txt`, the path of which can be specified using the `-neighbours` option. Each neighbour has a distance provided with the plus sign if the neighbour is reachable through the hypernym, otherwise, the minus sign is written.

```bash
java -jar target/babelnet-extract.jar -action neighbours -synsets "synsets.txt" -depth 2 -neighbours "neighbours.txt"
```

The format of the `synsets.txt` input file is the same as in the sense extraction action.

Building
--------

A couple of preliminary steps needs to be done before building this application with Maven. Firstly, it is necessary to download and unpack the [BabelNet-API-3.7.zip](http://babelnet.org/data/3.7/BabelNet-API-3.7.zip) archive. Secondly, two dependencies, `jltutils` and `babelnet-api`, need to be installed to the local Maven repository as follows.

```bash
mvn install:install-file -Dfile=lib/jltutils-2.2.jar -DgroupId=it.uniroma1.lcl.jlt -DartifactId=jltutils -Dversion=2.2 -Dpackaging=jar
unzip -p babelnet-api-3.7.jar META-INF/maven/it.uniroma1.lcl.babelnet/babelnet-api/pom.xml | grep -vP '<(scope|systemPath)>' >babelnet-api-3.7.pom
mvn install:install-file -Dfile=babelnet-api-3.7.jar -DpomFile=babelnet-api-3.7.pom
```

Note that the commands should be run inside the BabelNet API directory. The instructions are also available in Russian on NLPub: <https://nlpub.ru/BabelNet>. Having the preliminary setup completed, it is necessary to change the directory to `babelnet-extract` and then use Maven to compile and package the application.

```bash
mvn package
```

Other versions than BabelNet API 3.7 might also work, it is sufficient just to change the version value of the necessary BabelNet version in `pom.xml`.
