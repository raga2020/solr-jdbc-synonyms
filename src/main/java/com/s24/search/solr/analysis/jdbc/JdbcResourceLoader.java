package com.s24.search.solr.analysis.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

/**
 * {@link ResourceLoader} which loads resources from a {@link JdbcReader}.
 */
class JdbcResourceLoader implements ResourceLoader {
   /**
    * Default {@link Charset}. Has to be the same as in the {@link SynonymFilterFactory}.
    */
   private static final Charset UTF8 = Charset.forName("UTF-8");

   /**
    * {@link ResourceLoader} to delegate class loading to.
    */
   private ResourceLoader parent;

   /**
    * Database based reader.
    */
   private final JdbcReader reader;

   /**
    * Constructor.
    * 
    * @param reader
    *           Database based reader.
    */
   public JdbcResourceLoader(ResourceLoader parent, JdbcReader reader) {
      this.parent = parent;
      this.reader = reader;
   }

   @Override
   public InputStream openResource(String resource) throws IOException {
      return new ReaderInputStream(reader.getReader(), UTF8);
   }

   @Override
   public <T> Class<? extends T> findClass(String cname, Class<T> expectedType) {
      return parent.findClass(cname, expectedType);
   }

   @Override
   public <T> T newInstance(String cname, Class<T> expectedType) {
      return parent.newInstance(cname, expectedType);
   }
}
