solr-jdbc-synonyms
==================

![travis ci build status](https://travis-ci.org/shopping24/solr-jdbc-synonyms.png)

A Solr synonym filter for reading synonyms out of JDBC. The `DataSource` to retrieve synonyms
from is injected via [JNDI](http://de.wikipedia.org/wiki/Java_Naming_and_Directory_Interface).

## Installing the synonym filter (Apache Tomcat)

* Place the `solr-jdbc-synonyms-<VERSION>-jar-with-dependencies.jar` in the `/lib` 
  directory of your Solr installation.
* Place the JAR with the JDBC driver of your database in the `/lib` directory of your
  Tomcat.
* Place file `solr.xml` in your Tomcat's `/conf/Catalina/localhost` directory with
  the following content. Insert your database settings. [Look here for more information on
  configuring a JDBC pool in Tomcat](http://tomcat.apache.org/tomcat-7.0-doc/jndi-datasource-examples-howto.html)

    <Context>
      <Resource name="jdbc/synonyms" auth="Container" 
         type="javax.sql.DataSource" factory="org.apache.tomcat.jdbc.pool.DataSourceFactory"
         maxActive="10" maxIdle="2" minIdle="1" maxWait="10000" 
         validationQuery="select 1" testWhileIdle="true" 
         username="YOUR_DATABASE_USERNAME" password="YOUR_DATABASE_PASSWORD"
         driverClassName="YOUR_JDBC_DRIVER_CLASSNAME" url="YOUR_JDBC_URL" />
    </Context>

## Configuring the synonym filter in your `schema.xml`

The `JdbcSynonymFilterFactory` behaves exactly like the Solr [`SynonymFilterFactory`](https://wiki.apache.org/solr/AnalyzersTokenizersTokenFilters#solr.SynonymFilterFactory),
except that it does load the synonyms from a JDBC database and not from a file resource.
Configure the filter in your Solr analyzer chain like this:

    <filter class="com.s24.search.solr.analysis.jdbc.JdbcSynonymFilterFactory"   
       sql="SELECT concat(left, '=>', array_to_string(right, ',')) as line FROM synonyms;" 
       jndiName="jdbc/synonyms" ignoreCase="false" expand="true" />

The filter takes two arguments over the [`SynonymFilterFactory`](https://wiki.apache.org/solr/AnalyzersTokenizersTokenFilters#solr.SynonymFilterFactory):
	
* `jndiName`: The JNDI name of your JDBC `DataSource` as configured in your `solr.xml` or
   `server.xml`. In the example above, this would be `jdbc/synonyms`.
   
* `sql`: A SQL statement returning valid Solr synonym lines in the first SQL result column.  
  * Valid synonym formats include `x=>a`, `x=>a,b,c`, `x,y=>a,b,c` or `x,a,b,c`.
  * You might have your left and right hand side of your synonym definitions stored
    in separate columns in your database. Use a `concat` function to create a
    valid synonm line.
    * In [PostgreSQL](http://www.postgresql.org/docs/9.3/static/functions-string.html), you might use `SELECT concat(lhs, '=>', rhs) as line FROM synonyms;`
    * In [PostgreSQL](http://www.postgresql.org/docs/9.3/static/functions-array.html) with arrays, you might use `SELECT concat(lhs, '=>', array_to_string(rhs, ',')) as line FROM synonyms;`
    * In [Mysql](http://dev.mysql.com/doc/refman/5.6/en/string-functions.html#function_concat) your might use `SELECT concat(lhs, '=>', rhs) as line FROM synonyms;`

A complete fieldtype might look like example:

	<fieldType name="synonym_test" class="solr.TextField">
         <analyzer>
            <tokenizer class="solr.PatternTokenizerFactory" pattern="[\s]+" />
            <filter class="com.s24.search.solr.analysis.jdbc.JdbcSynonymFilterFactory"   
               sql="SELECT concat(left, '=>', array_to_string(right, ',')) as line FROM synonyms;" 
               jndiName="jdbc/synonyms" ignoreCase="false" expand="true" />
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

Some link regarding Maven central deployment:

* http://central.sonatype.org/pages/ossrh-guide.html
* http://central.sonatype.org/pages/apache-maven.html
* http://central.sonatype.org/pages/working-with-pgp-signatures.html#generating-a-key-pair
* http://maven.apache.org/guides/mini/guide-encryption.html
* http://central.sonatype.org/pages/releasing-the-deployment.html
* https://oss.sonatype.org/#stagingRepositories

## License

This project is licensed under the [Apache License, Version 2](http://www.apache.org/licenses/LICENSE-2.0.html).
