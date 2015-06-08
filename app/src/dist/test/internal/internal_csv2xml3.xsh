# test of csv2xml using our own supplied column names
# Try with one row that exceeds the limit should trim it using -trim

csv2xml -colnames  {<["mycol1","mycol2","mycol3"]>} -trim <<EOF
a,b,c
d,e,f
g,h,"i"
"a string","another string","Final string"
"testing quotes",  "embedded quotes like excel"  ,"double "" quotes"
"embeded,comma","""started with double quotes"""
j,k,l,"Unexpected Column"
j,k,l,
EOF
