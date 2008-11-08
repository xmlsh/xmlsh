# test of xml2csv2

F=../../samples/data/books.csv
csv2xml -header -attr $F | xml2csv -header -attr
echo
csv2xml -header $F | xml2csv -header