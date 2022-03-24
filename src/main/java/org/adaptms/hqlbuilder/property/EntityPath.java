package org.adaptms.hqlbuilder.property;

/**
 * @author ppolyakov at 17.10.2021 19:08
 */
public class EntityPath {
    protected String previousPath;

    public EntityPath( String previousPath ) {
        this.previousPath = previousPath;
    }

    protected EntityPath addToPrevious( String property ) {
        return new EntityPath( ( getPath() != null ? getPath() + "." : "" ) + ( property != null ? property : "" ) );
    }

    protected String step( String property ) {
        return getPath() != null ? getPath() + "." + property : property;
    }

    public String getPath() { return previousPath; }

    /**
     * Create new PathProperty object from String path
     * @param path path to use
     * @return QueryPath for String
     */
    public static EntityPath fromString( String path ) {
        return new EntityPath( path );
    }
}
