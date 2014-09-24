# test of xml2json
import commands json
F=../../samples/data/youtube.json 
json2xml -format jsonx < $F |  xml2json -p -format jsonx 

