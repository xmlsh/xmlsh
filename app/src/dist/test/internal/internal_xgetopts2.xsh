# test of xgetopts

set -- -longa "Option A" -bc "Option BC" -bool -multi "Multi 1" -multi <[ <multi>Multi 2 as XML</multi> ]> Remaining <[ <args>Args</args> ]>

# Test with spaces before and after the , and :
PASS=$(xgetopts -a -o "a=longa:, multi:+ , missing: " -p "bool, bc: " -- "$@")
shift $?
echo PASS $PASS
echo "$@"

