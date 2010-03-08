# test of csv2xml using our own supplied column names
# Try with one row that exceeds the limit

csv2xml -colnames {<["col1","col2","col3"]>} <<EOF
a,b,c
d,e,f
g,h,"i"
"a string","another string","Final string"
"testing quotes",  "embedded quotes like excel"  ,"double "" quotes"
"embeded,comma","""started with double quotes"""
j,k,l,"Unexpected Column"
EOF
