package com.s24.search.solr.analysis.jdbc;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.Reader;
import java.io.StringReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * A configurable {@linkplain JdbcReader} that executes a given SQL statement on
 * a configured JNDI data source.
 *
 * @author Shopping24 GmbH, Torsten Bøgh Köster (@tboeghk)
 */
public class JndiJdbcReader implements JdbcReader {
   /**
    * Logger.
    */
   private static final Logger LOGGER = LoggerFactory.getLogger(JndiJdbcReader.class);

   /**
    * JNDI name of the data source.
    */
   private final String jndiName;

   /**
    * SQL to load synonyms.
    */
   private final String sql;

   /**
    * Ignore a missing database?.
    */
   private final boolean ignore;

   /**
    * The data source.
    */
   private DataSource dataSource = null;

   /**
    * Constructor.
    *
    * @param jndiName
    *           JNDI name.
    * @param sql
    *           SQL.
    * @param ignoreMissingDatabase
    *           Ignore a missing database?.
    */
   public JndiJdbcReader(String jndiName, String sql, boolean ignore) {
      this.jndiName = fixJndiName(checkNotNull(jndiName));
      this.sql = checkNotNull(sql);
      this.ignore = ignore;

      initDatabase();
   }

   /**
    * Add prefix "java:comp/env/" to the JNDI name, if it is missing.
    *
    * @param jndiName
    *           JNDI name.
    */
   private static String fixJndiName(String jndiName) {
      return jndiName.startsWith("java:comp/env/")? jndiName : "java:comp/env/" + jndiName;
   }

   /**
    * Initializes the database and lookups a {@linkplain DataSource} in JNDI.
    */
   protected void initDatabase() {
      try {
         Context ctx = new InitialContext();
         LOGGER.info("Looking up data source {} in JNDI.", jndiName);
         this.dataSource = (DataSource) ctx.lookup(jndiName);
         ctx.close();
      } catch (NameNotFoundException e) {
         LOGGER.error("Data source {} not found.", jndiName, e);
         if (!ignore) {
            throw new IllegalArgumentException("Missing data source.", e);
         }
      } catch (NamingException e) {
         LOGGER.error("JNDI error.", e);
         throw new IllegalArgumentException("JNDI error.", e);
      } catch (ClassCastException e) {
         LOGGER.error("The JNDI resource {} is no data source.", jndiName, e);
         throw new IllegalArgumentException("The JNDI resource is no data source.", e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Reader getReader() {
      if (dataSource == null) {
         if (ignore) {
            return new StringReader("");
         }
         throw new IllegalArgumentException("Missing data source.");
      }

      QueryRunner runner = new QueryRunner(dataSource);
      try {
         LOGGER.info("Querying for synonyms using {}", sql);
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
         LOGGER.info("Loaded {} synonyms", content.size());

         // return joined
         return new StringReader(Joiner.on('\n').join(content));
      } catch (SQLException e) {
         throw new IllegalArgumentException("Failed to load synonyms from the database", e);
      }
   }
}
