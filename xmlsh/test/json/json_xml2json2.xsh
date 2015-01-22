# test of xml2json
import module json
F=../../samples/data/youtube.json 
json2xml -format jsonx < $F |  xml2json -indent-json -format jsonx 

