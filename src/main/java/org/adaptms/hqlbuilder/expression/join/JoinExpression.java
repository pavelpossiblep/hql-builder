package org.adaptms.hqlbuilder.expression.join;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;

import java.util.Locale;

/**
 * @author ppolyakov at 24.03.2022 22:32
 */
public class JoinExpression extends AbstractExpression {

    @Getter( AccessLevel.PROTECTED ) private final JoinType joinType;
    @Getter( AccessLevel.PROTECTED ) private final String joinEntityClass;
    @Getter( AccessLevel.PROTECTED ) private final String joinEntityAlias;
    @Getter( AccessLevel.PROTECTED ) private final CommonWhereExpression withExpression;

    public JoinExpression( @NonNull JoinType type, @NonNull String joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
        super( ExpressionType.JOIN );
        this.joinType = type;
        this.joinEntityClass = joinEntityClass;
        this.joinEntityAlias = joinEntityAlias;
        this.withExpression = withExpression;
    }

    public void init( HQLBuilder builder ) {
        getWithExpression().init( builder );
    }

    @Override
    public String build() {
        return getJoinType().name().toLowerCase( Locale.ROOT ) + " join " + getJoinEntityClass() + " " + getJoinEntityAlias()
                + " with " + getWithExpression().build();
    }
}
