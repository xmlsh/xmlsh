# Test schematron validation
# same as xvalidate -schematron

echo Schematron validation
N=$(schematron ../../samples/data/books.sch ../../samples/data/books.xml | xmlns:svrl=http://purl.oclc.org/dsdl/svrl xpath 'count(//svrl:fired-rule)')
echo Matching Items: $N
