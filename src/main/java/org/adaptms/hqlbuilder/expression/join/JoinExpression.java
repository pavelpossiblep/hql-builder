package org.adaptms.hqlbuilder.expression.join;

import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;

import java.util.Locale;

/**
 * @author ppolyakov at 24.03.2022 22:32
 */
public class JoinExpression extends AbstractExpression {

    private final JoinType joinType;
    private final String joinEntityClass;
    private final String joinEntityAlias;
    private final CommonWhereExpression withExpression;

    public JoinExpression( JoinType type, String joinEntityClass, String joinEntityAlias, CommonWhereExpression withExpression ) {
        super( ExpressionType.JOIN );

        if ( null == joinEntityClass || joinEntityClass.isEmpty() ) throw new IllegalArgumentException( "Join entity class may not be empty." );

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

    protected JoinType getJoinType() {
        return joinType;
    }

    protected String getJoinEntityClass() {
        return joinEntityClass;
    }

    protected String getJoinEntityAlias() {
        return joinEntityAlias;
    }

    protected CommonWhereExpression getWithExpression() {
        return withExpression;
    }
}
