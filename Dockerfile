FROM maven:3-jdk-8

MAINTAINER Dmitry Ustalov <dmitry.ustalov@gmail.com>

ENV BABELNET=3.7 \
MAVEN_OPTS=-Dmaven.test.skip=true\ -Dmaven.javadoc.skip=true\ -Dmaven.repo.local=/babelnet/.m2\ -Duser.home=/var/maven \
MAVEN_CONFIG=/var/maven/.m2

RUN \
apt-get update && \
apt-get install -y -o Dpkg::Options::="--force-confold" --no-install-recommends wordnet-base && \
apt-get clean && \
rm -rf /var/lib/apt/lists/*

RUN \
mkdir -p $MAVEN_CONFIG && \
wget -q "https://github.com/nlpub/babelnet-extract/releases/download/bn37/BabelNet-API-$BABELNET.zip" && \
unzip BabelNet-API-$BABELNET.zip && \
rm -fv BabelNet-API-$BABELNET.zip && \
mv -fv BabelNet-API-$BABELNET /babelnet && \
mvn install:install-file -Dfile=/babelnet/lib/jltutils-2.2.jar -DgroupId=it.uniroma1.lcl.jlt -DartifactId=jltutils -Dversion=2.2 -Dpackaging=jar && \
unzip -p /babelnet/babelnet-api-$BABELNET.jar META-INF/maven/it.uniroma1.lcl.babelnet/babelnet-api/pom.xml | grep -vP '<(scope|systemPath)>' >/babelnet/babelnet-api-$BABELNET.pom && \
mvn install:install-file -Dfile=/babelnet/babelnet-api-$BABELNET.jar -DpomFile=/babelnet/babelnet-api-$BABELNET.pom && \
mkdir -p /babelnet/index /opt/WordNet && \
sed -re 's|^# babelnet.dir=.*$|babelnet.dir=/babelnet/index|' -i /babelnet/config/babelnet.var.properties && \
ln -sf /usr/share/wordnet /opt/WordNet/dict

RUN \
wget -q "https://github.com/nlpub/babelnet-extract/archive/master.zip" && \
unzip master.zip && \
rm -fv master.zip && \
mv -fv babelnet-extract-master /babelnet/extract && \
cd /babelnet/extract && \
mvn package && \
mv /babelnet/extract/target/babelnet-extract.jar /babelnet/extract/babelnet-extract.jar && \
mvn clean && \
ln -sfT /babelnet/config /babelnet/extract/config && \
ln -sfT /babelnet/resources /babelnet/extract/resources && \
echo '#!/bin/sh\njava -jar /babelnet/extract/babelnet-extract.jar $@' >/bin/babelnet-extract && \
chmod +x /bin/babelnet-extract

WORKDIR /babelnet
