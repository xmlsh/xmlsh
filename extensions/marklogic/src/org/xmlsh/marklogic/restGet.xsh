# rest-get [-user] [-port] [-password] url query-params
. $(xuri -r /org/xmlsh/marklogic/resources/common.xsh)
_opts=$<(xgetopts -i "host:,port:,scheme:,uri:,path:" -s -o "u=user:,p=password:" -- $@)
_popts=$<(xgetopts -a -p "host:,port:,scheme:,uri:,path:" -ps -i "u=user:,p=password:" -- $@)
shift $?
uri=$(:rest-uri $_popts $@)

MLUSER=getopt($_opts u $MLUSER)
MLPASSWORD=getopt($_opts p $MLPASSWORD)

[ -n "$MLUSER" ] && _up=(-user $MLUSER)
[ -n "$MLPASSWORD" ] && _up=($_up -password $MLPASSWORD)





http $_up  -get "$uri"  