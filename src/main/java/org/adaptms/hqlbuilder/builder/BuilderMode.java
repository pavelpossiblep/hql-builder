package org.adaptms.hqlbuilder.builder;

/**
 * @author ppolyakov at 24.03.2022 16:18
 */
public enum BuilderMode {
    SELECT( "from", true ),
//    INSERT( "into", false ),
    UPDATE( null, false ),
    DELETE( null, false );

    private final String additionalKeyword;
    private final boolean needsAlias;

    BuilderMode( String additionalKeyword, boolean needsAlias ) {
        this.additionalKeyword = additionalKeyword;
        this.needsAlias = needsAlias;
    }

    public String getAdditionalKeyword() {
        return additionalKeyword;
    }

    public boolean isNeedsAlias() {
        return needsAlias;
    }
}
