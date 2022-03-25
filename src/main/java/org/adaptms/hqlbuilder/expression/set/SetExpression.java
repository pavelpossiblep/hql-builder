package org.adaptms.hqlbuilder.expression.set;

import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.ExpressionType;
import org.adaptms.hqlbuilder.expression.where.CommonWhereExpression;

import java.text.MessageFormat;

/**
 * @author ppolyakov at 24.03.2022 23:33
 */
public class SetExpression extends CommonWhereExpression {

    private final String path;
    private final Object value;
    private String variableName;

    public SetExpression( String path, Object value ) {
        super( ExpressionType.SET );

        this.path = path;
        this.value = value;
    }

    @Override
    public void init( HQLBuilder builder ) {
        this.variableName = processVariable( getValue(), builder );
    }

    @Override
    public String build() {
        return MessageFormat.format( getType().getTemplate(), path, variableName );
    }

    public String getPath() {
        return path;
    }

    protected Object getValue() {
        return value;
    }

    protected String getVariableName() {
        return variableName;
    }
}
