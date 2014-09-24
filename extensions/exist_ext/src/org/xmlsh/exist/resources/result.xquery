(: convert result of exist using xquery :)
declare namespace exist="http://exist.sourceforge.net/NS/exist";
declare variable $meta as xs:boolean external;


declare function local:value( $type as xs:string , $value ) 
{


    if ($type eq 'xs:untypedAtomic') then xs:untypedAtomic($value)
 else if ($type eq 'xs:anyURI') then xs:anyURI($value)
 else if ($type eq 'xs:ENTITY') then xs:ENTITY($value)
 else if ($type eq 'xs:ID') then xs:ID($value)
 else if ($type eq 'xs:NMTOKEN') then xs:NMTOKEN($value)
 else if ($type eq 'xs:language') then xs:language($value)
 else if ($type eq 'xs:NCName') then xs:NCName($value)
 else if ($type eq 'xs:Name') then xs:Name($value)
 else if ($type eq 'xs:token') then xs:token($value)
 else if ($type eq 'xs:normalizedString') then xs:normalizedString($value)
 else if ($type eq 'xs:string') then xs:string($value)
 else if ($type eq 'xs:QName') then xs:QName($value)
 else if ($type eq 'xs:boolean') then xs:boolean($value)
 else if ($type eq 'xs:base64Binary') then xs:base64Binary($value)
 else if ($type eq 'xs:hexBinary') then xs:hexBinary($value)
 else if ($type eq 'xs:byte') then xs:byte($value)
 else if ($type eq 'xs:short') then xs:short($value)
 else if ($type eq 'xs:int') then xs:int($value)
 else if ($type eq 'xs:long') then xs:long($value)
 else if ($type eq 'xs:unsignedByte') then xs:unsignedByte($value)
 else if ($type eq 'xs:unsignedShort') then xs:unsignedShort($value)
 else if ($type eq 'xs:unsignedInt') then xs:unsignedInt($value)
 else if ($type eq 'xs:unsignedLong') then xs:unsignedLong($value)
 else if ($type eq 'xs:positiveInteger') then xs:positiveInteger($value)
 else if ($type eq 'xs:nonNegativeInteger') then xs:nonNegativeInteger($value)
 else if ($type eq 'xs:negativeInteger')     then xs:negativeInteger($value)
 else if ($type eq 'xs:nonPositiveInteger')   then xs:nonPositiveInteger($value)
 else if ($type eq 'xs:integer') then xs:integer($value)
 else if ($type eq 'xs:decimal') then xs:decimal($value)
 else if ($type eq 'xs:float') then xs:float($value)
 else if ($type eq 'xs:double') then xs:double($value)
 else if ($type eq 'xs:date') then xs:date($value)
 else if ($type eq 'xs:time') then xs:time($value)
 else if ($type eq 'xs:dateTime') then xs:dateTime($value)
 else if ($type eq 'xs:dayTimeDuration' ) then xs:dayTimeDuration($value)
 else if ($type eq 'xs:yearMonthDuration' )   then xs:yearMonthDuration($value)
 else if ($type eq 'xs:duration') then xs:duration($value)
 else if ($type eq 'xs:gMonth') then xs:gMonth($value)
 else if ($type eq 'xs:gYear') then xs:gYear($value)
 else if ($type eq 'xs:gYearMonth') then xs:gYearMonth($value)
 else if ($type eq 'xs:gDay') then xs:gDay($value)
 else if ($type eq 'xs:gMonthDay') then xs:gMonthDay($value)
 else $value



};



typeswitch( /node() )
case    $e as element(exist:result) 
    return (
        if( $meta ) then <exist:result>{$e/@*}</exist:result> else (),
        for $n in $e/* 
        return
            typeswitch( $n )
            case    element(exist:value) return local:value( $n/@exist:type , $n/node() )
            default return $n
    ) 
        
       
default 
    return / 

    