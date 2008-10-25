# test of csv2xml

x1=$<(echo foo,bar,spam | csv2xml)
echo $x1 | xpath '/root/row/col[2]'

F=../../samples/data/books.csv
csv2xml -root books -row item -header -attr $F 