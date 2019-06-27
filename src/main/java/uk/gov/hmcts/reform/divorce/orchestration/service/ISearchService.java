package uk.gov.hmcts.reform.divorce.orchestration.service;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public interface ISearchService {
    SearchSourceBuilder buildBooleanSearchSource(int start, int batchSize, QueryBuilder... builders);
}
