package org.adaptms.hqlbuilder.expression.column;

/**
 * @author ppolyakov at 24.03.2022 16:52
 */
public enum ColumnExpressionType {
    DEFAULT( "{0}" ),
    COUNT( "count({0})" ),
    DISTINCT( "distinct {0}" ),
    COUNT_DISTINCT( "count(distinct {0})" );

    private final String query;

    ColumnExpressionType( String query ) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }
}
