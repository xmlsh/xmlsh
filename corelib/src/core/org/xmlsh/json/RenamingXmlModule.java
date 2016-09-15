package org.xmlsh.json;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.xmlsh.util.INamingStrategy;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.MinimalClassNameIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlAnnotationIntrospector;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlTypeResolverBuilder;

@SuppressWarnings("serial")
final class RenamingXmlModule extends JacksonXmlModule {
    /*
     * Shared Object Mapper - uses default configuration so needs to be copied if custom configs are used
     * 
     */
    private INamingStrategy mNamingStrategy = INamingStrategy.DefaultNamingStrategy;
    class RenamingXmlTypeResolverBuilder extends XmlTypeResolverBuilder {

        // derive from super.super 
        private class RenamingMinimalClassNameIdResolver extends
               MinimalClassNameIdResolver {
            private RenamingMinimalClassNameIdResolver(JavaType baseType,
                    TypeFactory typeFactory) {
                super(baseType, typeFactory);
            }

            // SNH
            @SuppressWarnings("deprecation")
            @Override
            public JavaType typeFromId(String id) {
                return this.typeFromId(null,id);
            }
            
            @Override
            public JavaType typeFromId(DatabindContext context, String id) {
                return super.typeFromId(context,  mNamingStrategy.fromXmlName(new QName(id)));
                   
            }
        }
        // derive from super.super 
        private class RenamingClassNameIdResolver extends
                ClassNameIdResolver {
            private RenamingClassNameIdResolver(JavaType baseType,
                    TypeFactory typeFactory) {
                super(baseType, typeFactory);
            }

            // SNH
            @SuppressWarnings("deprecation")
            @Override
            public JavaType typeFromId(String id) {
                return this.typeFromId(null,id);
            }
            
            @Override
            public JavaType typeFromId(DatabindContext context, String id) {
                return super.typeFromId(context, mNamingStrategy.toXmlName(id).getLocalPart());
            }
        }

        @Override
        protected TypeIdResolver idResolver(MapperConfig<?> config,
                JavaType baseType, Collection<NamedType> subtypes,
                boolean forSer, boolean forDeser) {
            if (_customIdResolver != null) {
                return _customIdResolver;
            }
            // Only override handlings of class, minimal class; name is good as is
            switch (_idType) {
            case CLASS:
                return new RenamingClassNameIdResolver(baseType, config.getTypeFactory());
            case MINIMAL_CLASS:
                return new RenamingMinimalClassNameIdResolver(baseType, config.getTypeFactory());
            default:
                return super.idResolver(config, baseType, subtypes, forSer, forDeser);
            }
        }
    }
    
    
    @SuppressWarnings("serial")
    @Override
    protected AnnotationIntrospector _constructIntrospector() {
        return new JacksonXmlAnnotationIntrospector(_cfgDefaultUseWrapper) {
            @Override
            protected StdTypeResolverBuilder _constructStdTypeResolverBuilder() {
                return  new RenamingXmlTypeResolverBuilder();
            }
            
        };
    }
}