# rename a marklogic document
# rest-get [-user] [-port] [-password] url query-params
_opts=$<(xgetopts -i "host:,port:,scheme:,uri:,path:" -s -o "u=user:,p=password:" -- "$@")
_popts=$<(xgetopts -a -p "host:,port:,scheme:,uri:,path:" -ps -i "u=user:,p=password:" -- "$@")



uri=$(rest-uri $_popts)


http -get $uri