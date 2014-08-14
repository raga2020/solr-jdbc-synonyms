solr-jdbc-synonyms
==================

![travis ci build status](https://travis-ci.org/shopping24/solr-jdbc-synonyms.png)

A Solr synonym filter for reading synonyms out of JDBC. You have to manage your Datasource by the famous [JNDI-Interface](http://de.wikipedia.org/wiki/Java_Naming_and_Directory_Interface).

## Installing the synonym filter

## Configuring the synonym filter

As an example you can simply put a new fieldtype in your Solr schema. The minimum required arguments are:
	
* sql: Just type your select statement. The output for each line has to be in the solr-synonym-format: x=>y or x=>v,w,x,y,z …. The following statement will work in postgres: 
*SELECT concat(left, '=>', array\_to\_string(right, ',')) as line FROM synonyms;*
	
* jndiName: As the name said. Your JNDI name.
* note: all other notes of the SolrSynonymFilter will work as expected.

A complete fieldtype example:

	<fieldType name="synonym_test" class="solr.TextField">
         <analyzer>
            <tokenizer class="solr.PatternTokenizerFactory" pattern="[\s]+" />
            <filter class="com.s24.search.solr.analysis.jdbc.JdbcSynonymFilterFactory"   
               sql="SELECT concat(left, '=>', array_to_string(right, ',')) as line FROM synonyms;" 
               jndiName="jdbc/jndiname"
               ignoreCase="false"
               expand="true" />
         </analyzer>
      </fieldType>

## Building the project

This should install the current version into your local repository

    $ export JAVA_HOME=$(/usr/libexec/java_home -v 1.7)
    $ export MAVEN_OPTS="-Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -Dmaven.wagon.http.ssl.ignore.validity.dates=true"
    $ mvn clean install
    
### Releasing the project to maven central
    
Define new versions
    
    $ export NEXT_VERSION=<version>
    $ export NEXT_DEVELOPMENT_VERSION=<version>-SNAPSHOT

Then execute the release chain

    $ mvn org.codehaus.mojo:versions-maven-plugin:2.0:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_VERSION
    $ git commit -a -m "pushes to release version $NEXT_VERSION"
    $ mvn clean deploy -P release
    $ git tag -a v$NEXT_VERSION -m "`curl -s http://whatthecommit.com/index.txt`"
    $ mvn org.codehaus.mojo:versions-maven-plugin:2.0:set -DgenerateBackupPoms=false -DnewVersion=$NEXT_DEVELOPMENT_VERSION
    $ git commit -a -m "pushes to development version $NEXT_DEVELOPMENT_VERSION"
    $ git push origin tag v$NEXT_VERSION && git push origin

Some link regarding deployment:

* http://central.sonatype.org/pages/ossrh-guide.html
* http://central.sonatype.org/pages/apache-maven.html
* http://central.sonatype.org/pages/working-with-pgp-signatures.html#generating-a-key-pair
* http://maven.apache.org/guides/mini/guide-encryption.html
* http://central.sonatype.org/pages/releasing-the-deployment.html
* https://oss.sonatype.org/#stagingRepositories

## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).
