# get-permissions uri ...
_popts=$<(xgetopts -a -p "c=connect:,t=text" -ps -- "$@")

shift $?


for uri ; do
   :query $_popts -q <{{

declare function local:rolename( $id ) 
{
 
xdmp:eval('
	import module "http://marklogic.com/xdmp/security" at "/MarkLogic/security.xqy";
	declare variable $id external;
	sec:get-role-names( $id )/string()' , 
	(xs:QName("id") , $id ) , 
  	<options xmlns="xdmp:eval">
		    <database>{xdmp:security-database()}</database>
	</options>)
 
} ; 

  
declare variable $uri external ; 

<permissions uri="{$uri}">
{
for $perms in xdmp:document-get-permissions($uri)
return
	<permission capability="{$perms/sec:capability}" role="{local:rolename($perms/sec:role-id)}"/>
}
</permissions>
}}> -v uri $uri
 
done