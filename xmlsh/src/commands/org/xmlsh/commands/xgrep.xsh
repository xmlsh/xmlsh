# xgrep xpath [xml files]
EXPR=$1
shift
for f ; do
	xpath -e $EXPR < $f && echo $f
done