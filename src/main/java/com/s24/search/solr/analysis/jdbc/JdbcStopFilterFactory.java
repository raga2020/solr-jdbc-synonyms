package com.s24.search.solr.analysis.jdbc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

/**
 * 
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class JdbcStopFilterFactory extends StopFilterFactory {

   /**
    * {@link Charset} to encode synonym database with. Has to be the same as in
    * the {@link SynonymFilterFactory}.
    */
   private static final Charset UTF8 = Charset.forName("UTF-8");

   /**
    * Database based reader.
    */
   private final JdbcReader reader;

   /**
    * Constructor.
    *
    * @param args
    *           Configuration.
    */
   public JdbcStopFilterFactory(Map<String, String> args) {
      this(args, JndiJdbcReader.createFromSolrParams(args, "words"));
   }

   /**
    * Constructor.
    *
    * @param args
    *           Configuration.
    * @param reader
    *           Reader for synonyms.
    */
   JdbcStopFilterFactory(Map<String, String> args, JdbcReader reader) {
      super(args);

      this.reader = reader;
   }
   
   @Override
   public void inform(ResourceLoader loader) throws IOException {
      super.inform(new JdbcResourceLoader(loader, reader, UTF8));
   }

}
