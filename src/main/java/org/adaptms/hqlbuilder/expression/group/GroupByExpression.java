package org.adaptms.hqlbuilder.expression.group;

import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

/**
 * @author ppolyakov at 24.03.2022 23:12
 */
public class GroupByExpression extends AbstractExpression {

    private final String path;

    public GroupByExpression( String path ) {
        super( ExpressionType.GROUP_BY );

        if ( null == path || path.isEmpty() ) throw new IllegalArgumentException( "Path may not be empty." );

        this.path = path;
    }

    @Override
    public String build() {
        return path;
    }

    protected String getPath() {
        return path;
    }
}
