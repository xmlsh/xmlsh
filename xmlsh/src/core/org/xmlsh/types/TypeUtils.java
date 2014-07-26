package org.xmlsh.types;

public class TypeUtils
{
    public static final TypeFamily defaultFamily = TypeFamily.JAVA;
    private static final TypeFamily[] typeFamilyPrecidence = {
        TypeFamily.XTYPE,
        TypeFamily.JSON ,
        TypeFamily.XDM , 
        TypeFamily.JAVA 
    };
    
    // Make the best guess as to the type family given only an object
      public static TypeFamily inferFamily( Object obj ) {
          for( TypeFamily f : typeFamilyPrecidence )
              if( f.instance().isInstanceOfFamily(obj))
                  return f;

          return defaultFamily;
      }
                                                        
}
