# rest-uri 
#
_opts=$<(xgetopts -o "host:,port:,scheme:,uri:,path:" -a -noargs -- "$@")
shift $?

if [ $# -gt 0 ] ; then
   QUERY=$(xurlencode -q "$@")
fi


MLURI=$MLURI
MLURI=<[ ($_opts//option[@name eq "uri"]/string(),$MLURI)[1] ]>
# if MLURI or -uri is passed return it unchanged
if [ -z "$MLURI" ] ; then
  MLPORT=$(xuri -port $MLURI)
  MLSCHEME=$(xuri -scheme $MLURI)
  MLHOST=$(xuri -host $MLURI)
  MLPATH=$(xuri -path $MLPATH)
fi




MLPORT=$MLPORT
MLPORT=<[ ($_opts//option[@name eq "port"]/string(),$MLPORT)[1] ]>

MLPATH=$MLPATH
MLPATH=<[ ($_opts//option[@name eq "path"]/string(),$MLPATH)[1] ]>

MLHOST=$MLHOST
MLHOST=<[ ($_opts//option[@name eq "host"]/string(),$MLHOST)[1] ]>

MLSCHEME=$MLSCHEME
MLSCHEME=<[ ($_opts//option[@name eq "scheme"]/string(),$MLSCHEME)[1] ]>


xuri "$MLSCHEME" "" "$MLHOST" "$MLPORT" "$MLPATH" "$QUERY" ""


