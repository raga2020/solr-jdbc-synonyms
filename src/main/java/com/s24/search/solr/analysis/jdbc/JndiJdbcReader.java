package com.s24.search.solr.analysis.jdbc;

import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A configurable {@linkplain JdbcReader} that executes a given sql statement on
 * a configured JNDI datasource.
 * 
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class JndiJdbcReader implements JdbcReader {
   /**
    * Logger.
    */
   private static final Logger LOGGER = LoggerFactory.getLogger(JndiJdbcReader.class);

   /**
    * JNDI name.
    */
   private final String jndiName;

   /**
    * SQL.
    */
   private final String sql;

   /**
    * The data source.
    */
   private Optional<DataSource> dataSource;

   /**
    * Constructor.
    * 
    * @param jndiName
    *           JNDI name.
    * @param sql
    *           SQL.
    */
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
         LOGGER.info("Looking up {} in JNDI ...", jndiName);
         this.dataSource = Optional.of((DataSource) ctx.lookup(jndiName));
         ctx.close();
      } catch (Exception e) {
         this.dataSource = Optional.absent();
         LOGGER.error("The Datasource could not be retrieved because of ", e);
         // throw new IllegalArgumentException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Reader getReader() {

      if (!dataSource.isPresent()) {
         LOGGER.error("There is no correct datasource given. We will return no synonyms...");
         return new StringReader("");
      }

      QueryRunner runner = new QueryRunner(dataSource.get());
      try {

         // read lines off jdbc
         LOGGER.info("Querying for synonyms using {} ...", sql);
         List<String> content = runner.query(sql, new ResultSetHandler<List<String>>() {
            @Override
            public List<String> handle(ResultSet rs) throws SQLException {
               List<String> result = Lists.newArrayList();
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
