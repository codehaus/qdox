package com.thoughtworks.qdox.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.thoughtworks.qdox.JavaClassContext;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaSource;
import com.thoughtworks.qdox.model.ModelBuilder;
import com.thoughtworks.qdox.parser.Lexer;
import com.thoughtworks.qdox.parser.ParseException;
import com.thoughtworks.qdox.parser.impl.JFlexLexer;
import com.thoughtworks.qdox.parser.impl.Parser;

/**
 * This Library will immediately parse the source and keeps its reference to a private context.
 * Once the superclass explicitly asks for an instance if will be moved to the context f the supoerclass.
 * If there's a request to get a certain JavaModel Object from a SourceLibrary, it will check all ancestor SourceLibraries as well.
 * 
 * @author Robert Scholte
 * @since 2.0
 */
public class SourceLibrary
    extends AbstractClassLibrary
{
    // parser and unused JavaSources, JavaClasses and JavaPackages
    private JavaClassContext context = new JavaClassContext();
    
    private boolean debugLexer;

    private boolean debugParser;
    
    private String encoding = System.getProperty("file.encoding");

    /**
     * Create a new instance of SourceLibrary and chain it to the parent 
     * 
     * @param parent
     */
    public SourceLibrary( AbstractClassLibrary parent )
    {
        super( parent );
    }
    
    /**
     * Add a {@link Reader} containing java code to this library
     * 
     * @param reader a {@link Reader} which should contain java code
     * @return The constructed {@link JavaSource} object of this reader
     * @throws ParseException if this content couldn't be parsed to a JavaModel
     */
    public JavaSource addSource( Reader reader )
        throws ParseException
    {
        JavaSource source = parse( reader );
        registerJavaSource(source);
        return source;
    }

    /**
     * Add an {@link InputStream} containing java code to this library
     * 
     * @param stream an {@link InputStream} which should contain java code
     * @return The constructed {@link JavaSource} object of this stream
     * @throws ParseException if this content couldn't be parsed to a JavaModel
     */
    public JavaSource addSource( InputStream stream )
        throws ParseException
    {
        JavaSource source = parse( stream );
        registerJavaSource(source);
        return source;
    }
    
    /**
     * Add a {@link URL} containing java code to this library
     * 
     * @param url a {@link URL} which should contain java code
     * @return The constructed {@link JavaSource} object of this url
     * @throws ParseException if this content couldn't be parsed to a JavaModel
     */
    public JavaSource addSource( URL url )
        throws ParseException, IOException
    {
        return addSource( new InputStreamReader( url.openStream(), encoding) );
    }

    /**
     * Add a {@link File} containing java code to this library
     * 
     * @param file a {@link File} which should contain java code
     * @return The constructed {@link JavaSource} object of this file
     * @throws ParseException
     * @throws IOException
     */
    public JavaSource addSource( File file )
        throws ParseException, IOException
    {
        return addSource( new FileInputStream( file ) );
    }

    protected JavaSource parse( Reader reader )
        throws ParseException
    {
        try {
            return parse( new JFlexLexer( reader ) );
        }
        finally {
            try
            {
                reader.close();
            }
            catch ( IOException e ) {
            }
        }
    }

    protected JavaSource parse( InputStream stream )
        throws ParseException
    {
        try {
            return parse( new JFlexLexer( stream ) );
        }
        finally {
            try
            {
                stream.close();
            }
            catch ( IOException e ) {
            }
        }
    }

    private JavaSource parse( Lexer lexer )
        throws ParseException
    {
        JavaSource result = null;
        ModelBuilder builder = getModelBuilder();
        Parser parser = new Parser( lexer, builder );
        parser.setDebugLexer( debugLexer );
        parser.setDebugParser( debugParser );
        if ( parser.parse() )
        {
            result = builder.getSource();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    protected JavaClass resolveJavaClass( String name )
    {
        // abstractLibrary only calls this when it can't find the source itself.
        // it will take over the reference
        return (JavaClass) context.removeClassByName( name );
    }
    
    private void registerJavaSource(JavaSource source) {
        context.add( source );
        context.add( source.getPackage() );

        for( JavaClass clazz : source.getClasses()) {
            registerJavaClass( clazz );
        }
    }
    
    //@todo move to JavaClassContext
    private void registerJavaClass(JavaClass clazz) {
        if (clazz != null) {
            context.add( clazz );
        }
        for( int clazzIndex = 0; clazzIndex < clazz.getNestedClasses().length; clazzIndex++ ) {
            registerJavaClass( clazz.getNestedClasses()[clazzIndex] );
        }
    }

    /**
     * Use the Lexer in debug mode
     * 
     * @param debugLexer 
     */
    public void setDebugLexer( boolean debugLexer )
    {
        this.debugLexer = debugLexer;
    }
    
    /**
     * Use the Parser in debug mode
     * 
     * @param debugParser
     */
    public void setDebugParser( boolean debugParser )
    {
        this.debugParser = debugParser;
    }
    
    /**
     * Sets the encoding to use when parsing a URL or InputStreamReader
     * 
     * @param encoding
     */
    public void setEncoding( String encoding )
    {
        this.encoding = encoding;
    }
    
    /**
     * Get all classes, including those from parent SourceLibraries
     */
    public List<JavaClass> getJavaClasses()
    {
        List<JavaClass> result = new ArrayList<JavaClass>();
        List<JavaClass> unusedClasses = context.getClasses();
        List<JavaClass> usedClasses = getJavaClasses( new ClassLibraryFilter()
        {
            public boolean accept( AbstractClassLibrary classLibrary )
            {
                return (classLibrary instanceof SourceLibrary);
            }
        });
        result.addAll( usedClasses );
        result.addAll( unusedClasses );
        return Collections.unmodifiableList( result );
    }

    /**
     * Get all packages, including those from parent SourceLibraries
     */
    public List<JavaPackage> getJavaPackages()
    {
        List<JavaPackage> result = new ArrayList<JavaPackage>();
        List<JavaPackage> unusedPackages = context.getPackages();
        List<JavaPackage> usedPackages = getJavaPackages( new ClassLibraryFilter()
        {
            public boolean accept( AbstractClassLibrary classLibrary )
            {
                return (classLibrary instanceof SourceLibrary);
            }
        });
        result.addAll( usedPackages );
        result.addAll( unusedPackages );
        return Collections.unmodifiableList( result );
    }
    
    /**
     * Get all sources, including those from parent SourceLibraries
     */
    public List<JavaSource> getJavaSources()
    {
        List<JavaSource> result = new ArrayList<JavaSource>();
        List<JavaSource> unusedSources = context.getSources();
        List<JavaSource> usedSources = getJavaSources( new ClassLibraryFilter()
        {
            public boolean accept( AbstractClassLibrary classLibrary )
            {
                return (classLibrary instanceof SourceLibrary);
            }
        });
        result.addAll( usedSources );
        result.addAll( unusedSources );
        return Collections.unmodifiableList( result );
    }
    
    /**
     * {@inheritDoc}
     */
    protected boolean containsClassReference( String name )
    {
        return context.getClassByName( name ) != null;
    }
}
