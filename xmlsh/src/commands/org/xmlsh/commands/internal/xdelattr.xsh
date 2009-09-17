# deletes an attribute
_ATTR=$1
shift
xed -d -matches "@$_ATTR" "$@"