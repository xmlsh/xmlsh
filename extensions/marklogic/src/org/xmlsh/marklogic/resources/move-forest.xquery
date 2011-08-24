xquery version '1.0-ml';

declare variable $uri as xs:string external ;

declare function local:document-move-forest($uri as xs:string)
{

  local:document-move-forest(
    $uri,
    xdmp:database-forests(xdmp:database())
  )

};

 

declare function local:document-move-forest($uri as xs:string,
  $forest-ids as xs:unsignedLong*)
{
    let $prop-ns := namespace-uri(<prop:properties/>) ,
        $properties :=
      xdmp:document-properties($uri)/node()[ namespace-uri(.) ne $prop-ns ] 
      
        
  return (
  xdmp:document-insert(
    $uri,
    fn:doc($uri),
    xdmp:document-get-permissions($uri),
    xdmp:document-get-collections($uri),
    xdmp:document-get-quality($uri),
    $forest-ids
  ),
  xdmp:document-set-properties($uri, $properties) 
  )
    
    
};

local:document-move-forest($uri)
