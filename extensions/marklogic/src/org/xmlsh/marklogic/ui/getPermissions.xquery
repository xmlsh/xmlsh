import module namespace sec="http://marklogic.com/xdmp/security" at 
    "/MarkLogic/security.xqy";
import module namespace hof="http://marklogic.com/higher-order"
       at "/MarkLogic/appservices/utils/higher-order.xqy";
declare namespace s="http://marklogic.com/xdmp/security" ;
declare variable $url as xs:string  external ; 
declare variable $secid := xdmp:security-database(xdmp:database());

  for $p in xdmp:document-get-permissions($url) 
  return hof:apply-in($secid,
                            function () {
                                       sec:get-role-names($p//s:role-id)
                              })  || ":" ||  $p//s:capability 
 				