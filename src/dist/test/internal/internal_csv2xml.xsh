# test of csv2xml
set +indent
x1=$<(echo foo,bar,spam | csv2xml)
xpath '/root/row/col[2]' <{x1} >{x2}
xcmp $x2 <[ <col>bar</col> ]> || echo Failed compare

F=../../samples/data/books.csv
csv2xml -root books -row item -header -attr $F 
