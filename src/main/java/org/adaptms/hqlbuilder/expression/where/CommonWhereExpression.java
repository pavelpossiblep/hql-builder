package org.adaptms.hqlbuilder.expression.where;

import org.adaptms.hqlbuilder.IBuildable;
import org.adaptms.hqlbuilder.builder.HQLBuilder;
import org.adaptms.hqlbuilder.expression.AbstractExpression;
import org.adaptms.hqlbuilder.expression.ExpressionType;
import org.adaptms.hqlbuilder.property.EntityPath;

import java.text.MessageFormat;
import java.util.Collection;

/**
 * @author ppolyakov at 24.03.2022 17:55
 */
public class CommonWhereExpression extends AbstractExpression {

    private final Object[] arguments;
    private String[] querySubstitutes;

    public CommonWhereExpression( ExpressionType type, Object... arguments ) {
        super( type );
        this.arguments = arguments;
    }

    public void init( HQLBuilder builder ) {
        if ( arguments == null ) return;

        if ( getType().getNumberOfArguments() != getArguments().length )
            throw new IllegalArgumentException( "Incorrect arguments amount. Expected: " + getType().getNumberOfArguments() + ", got: " + getArguments().length );

        this.querySubstitutes = new String[ getArguments().length ];

        for ( int i = 0; i < getQuerySubstitutes().length; i++ ) {
            this.getQuerySubstitutes()[i] = processVariable( getArguments()[i], builder );
        }
    }

    @Override
    public String build() {
        return MessageFormat.format( getType().getTemplate(), getQuerySubstitutes() );
    }

    protected String processVariable( Object input, HQLBuilder builder ) {
        if ( input instanceof EntityPath ) {
            return ( ( EntityPath ) input ).getPath();
        } else if ( input instanceof IBuildable ) {
            if ( input instanceof HQLBuilder ) {
                HQLBuilder incomingBuilder = ( HQLBuilder ) input;
                builder.getVariables().putAll( incomingBuilder.getVariables() );
            }
            return "(" + ( ( IBuildable ) input ).build() + ")";
        } else if ( input.getClass().isArray() || Collection.class.isAssignableFrom( input.getClass() ) ) {
            return "(:" + builder.addVariable( input ) + ")";
        } else {
            return  ":" + builder.addVariable( input );
        }
    }

    protected Object[] getArguments() {
        return arguments;
    }

    protected String[] getQuerySubstitutes() {
        return querySubstitutes;
    }
}
