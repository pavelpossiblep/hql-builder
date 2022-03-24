package org.adaptms.hqlbuilder.expression.group;

import lombok.AccessLevel;
import lombok.Getter;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;

/**
 * @author ppolyakov at 24.03.2022 23:12
 */
public class GroupByExpression extends AbstractExpression {

    @Getter( AccessLevel.PROTECTED ) private final String path;

    public GroupByExpression( String path ) {
        super( ExpressionType.GROUP_BY );

        this.path = path;
    }

    @Override
    public String build() {
        return path;
    }
}
