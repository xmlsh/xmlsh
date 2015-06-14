# test of xml2csv2
# test of xls command
import commands posix

csv2xml -header -tab -input-encoding utf16  < ../../samples/data/employee.txt |
xml2csv -header -tab -output-text-encoding utf8 
