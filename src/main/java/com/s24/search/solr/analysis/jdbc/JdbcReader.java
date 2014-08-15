package com.s24.search.solr.analysis.jdbc;

import java.io.Reader;

/**
 * Reads "lines" of configuration out of JDBC.
 *
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public interface JdbcReader {
   /**
    *
    * @return a {@linkplain Reader}, never <code>null</code>
    */
   Reader getReader();
}
