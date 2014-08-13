package com.s24.search.solr.analysis.jdbc;

import java.io.Reader;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.google.common.base.Preconditions;

/**
 * A configurable {@linkplain JdbcReader} that executes a given sql statement on
 * a configured JNDI datasource.
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class JndiJdbcReader implements JdbcReader {

   private final String sql;
   private final String jndiName;
   private DataSource dataSource;

   public JndiJdbcReader(String jndiName, String sql) {
      Preconditions.checkNotNull(jndiName);
      Preconditions.checkNotNull(sql);
      
      this.jndiName = jndiName;
      this.sql = sql;
      
      initDatabase();
   }
   
   
   protected void initDatabase() {
      try {
         Context ctx = new InitialContext();
      } catch (Exception e) {
         throw new IllegalArgumentException(e);
      }
   }
   
   
   @Override
   public Reader getReader() {
      // TODO Auto-generated method stub
      return null;
   }

}
