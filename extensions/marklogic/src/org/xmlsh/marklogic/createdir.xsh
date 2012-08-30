# create  uri ...
_opts=$<(xgetopts -a -p "c=connect:,t=text" -ps -- "$@")

shift $?

for uri ; do
   :query $_opts -q <{{
		xquery version "1.0-ml";
		declare variable $uri as xs:string external ; 
		xdmp:directory-create($uri)
	}}> -v uri $uri
done