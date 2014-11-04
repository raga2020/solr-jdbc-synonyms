package com.s24.search.solr.analysis;

import org.apache.solr.search.SolrIndexSearcher;

/**
 * Interface for event notification, when a new searcher has been created.
 */
public interface SearcherAware {
   /**
    * Notification that a new searcher has been created.
    * 
    * @param searcher
    *           The new searcher.
    */
   void informNewSearcher(SolrIndexSearcher searcher);
}
