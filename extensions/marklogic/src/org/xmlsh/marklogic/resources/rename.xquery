declare variable $src as xs:string external ;
declare variable $target as xs:string external ;

declare function local:document-rename(
   $old-uri as xs:string, $new-uri as xs:string)
  as empty-sequence()
{
    xdmp:document-delete($old-uri)
    ,
    let $permissions := xdmp:document-get-permissions($old-uri)
    let $collections := xdmp:document-get-collections($old-uri)
    return xdmp:document-insert(
      $new-uri, doc($old-uri),
      if ($permissions) then $permissions
      else xdmp:default-permissions(),
      if ($collections) then $collections
      else xdmp:default-collections(),
      xdmp:document-get-quality($old-uri)
    )
    ,
    let $prop-ns := namespace-uri(<prop:properties/>)
    let $properties :=
      xdmp:document-properties($old-uri)/node()
        [ namespace-uri(.) ne $prop-ns ]
    return xdmp:document-set-properties($new-uri, $properties) 
 };

local:document-rename($src,$target)