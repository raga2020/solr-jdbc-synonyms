package com.s24.search.solr.analysis.jdbc;

import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A configurable {@linkplain JdbcReader} that executes a given sql statement on
 * a configured JNDI datasource.
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class JndiJdbcReader implements JdbcReader {

   private final Logger logger = LoggerFactory.getLogger(getClass());

   private final String sql;
   private final String jndiName;
   private DataSource dataSource;

   public JndiJdbcReader(String jndiName, String sql) {
      Preconditions.checkNotNull(jndiName);
      Preconditions.checkNotNull(sql);

      this.sql = sql;

      // fix jndi name
      if (!jndiName.startsWith("java:comp/env/")) {
         this.jndiName = "java:comp/env/" + jndiName;
      } else {
         this.jndiName = jndiName;
      }

      initDatabase();
   }

   /**
    * Initializes the database and lookups a {@linkplain DataSource} in JNDI.
    */
   protected void initDatabase() {
      try {
         Context ctx = new InitialContext();
         logger.info("Looking up {} in JNDI ...", jndiName);
         this.dataSource = (DataSource) ctx.lookup(jndiName);
         ctx.close();
      } catch (Exception e) {
         throw new IllegalArgumentException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Reader getReader() {
      QueryRunner runner = new QueryRunner(dataSource);
      try {

         // read lines off jdbc
         logger.info("Querying for synonyms using {} ...", sql);
         List<String> content = runner.query(sql, new ResultSetHandler<List<String>>() {
            @Override
            public List<String> handle(ResultSet rs) throws SQLException {
               ArrayList<String> result = Lists.newArrayList();
               while (rs.next()) {
                  result.add(rs.getString(1));
               }
               return result;
            }
         });

         // return joined
         return new StringReader(Joiner.on('\n').join(content));
      } catch (SQLException e) {
         throw new RuntimeException(e);
      }
   }
}
