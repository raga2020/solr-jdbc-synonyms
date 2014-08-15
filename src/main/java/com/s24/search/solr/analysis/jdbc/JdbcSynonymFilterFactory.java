package com.s24.search.solr.analysis.jdbc;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.lucene.analysis.synonym.SynonymFilter;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

/**
 * Factory for a {@link SynonymFilter} which loads synonyms from a database.
 */
public class JdbcSynonymFilterFactory extends SynonymFilterFactory {
   /**
    * Parameter: JNDI name of data source.
    */
   public static final String JNDI_NAME_PARAM = "jndiName";

   /**
    * Parameter: SQL to load synonyms.
    */
   public static final String SQL_PARAM = "sql";

   /**
    * Parameter: Ignore a missing database?.
    */
   public static final String IGNORE_PARAM = "ignoreMissingDatabase";

   /**
    * {@link Charset} to encode synonym database with.
    * Has to be the same as in the {@link SynonymFilterFactory}.
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
   public JdbcSynonymFilterFactory(Map<String, String> args) {
      this(args, createReader(args));
   }

   /**
    * Constructor.
    *
    * @param args
    *           Configuration.
    * @param reader
    *           Reader for synonyms.
    */
   JdbcSynonymFilterFactory(Map<String, String> args, JdbcReader reader) {
      super(args);

      this.reader = reader;
   }

   /**
    * Create the {@link JndiJdbcReader}.
    * Set synonyms file to a fixed name.
    * This is needed because our patched resource loader should load the synonyms exactly once.
    *
    * @param args
    *           Configuration.
    * @return Configuration.
    */
   private static JndiJdbcReader createReader(Map<String, String> args) {
      // Set a fixed synonyms "file".
      // This "file" will be loaded from the database by the JdbcResourceLoader.
      args.put("synonyms", JdbcResourceLoader.DATABASE);

      String name = args.remove(JNDI_NAME_PARAM);
      String sql = args.remove(SQL_PARAM);
      String ignore = args.remove(IGNORE_PARAM);
      return new JndiJdbcReader(name, sql, "true".equals(ignore));
   }

   @Override
   public void inform(ResourceLoader loader) throws IOException {
      super.inform(new JdbcResourceLoader(loader, reader, UTF8));
   }
}
