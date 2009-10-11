# test of xinclude

# Basic test
echo Validating with DTD
xvalidate  -dtd ../../samples/data/books.dtd < ../../samples/data/books.xml || echo Validation failed

echo Validating with xsd
xvalidate  -xsd ../../samples/data/books.xsd  ../../samples/data/books.xml || echo Validation failed


echo validate  -rng ../../samples/data/books.rng  ../../samples/data/books.xml || echo Validation failed

echo Following should fail
xvalidate  -xsd ../../samples/data/books.xsd  ../../samples/data/othello.xml  2>/dev/null || echo Successfully trapped invalid schema

echo Schematron validation
N=$(schematron ../../samples/data/books.sch ../../samples/data/books.xml | xmlns:svrl=http://purl.oclc.org/dsdl/svrl xpath 'count(//svrl:fired-rule)')
echo Matching Items: $N

N=$(xvalidate -schematron ../../samples/data/books.sch ../../samples/data/books.xml | xmlns:svrl=http://purl.oclc.org/dsdl/svrl xpath 'count(//svrl:fired-rule)')
echo Matching Items: $N

echo RNG Validation
xvalidate -rng ../../samples/data/books.rng ../../samples/data/books.xml || { echo failed RNG validation ; exit 1 ; }



