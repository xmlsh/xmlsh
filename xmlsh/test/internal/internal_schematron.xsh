# Test schematron validation
# same as xvalidate -schematron

echo Schematron validation
declare namespace svrl=http://purl.oclc.org/dsdl/svrl 
N=$(schematron ../../samples/data/books.sch ../../samples/data/books.xml | 
     xpath 'count(//svrl:fired-rule)')
echo Matching Items: $N
