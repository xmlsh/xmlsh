# test of xinclude

# Basic test
echo Validating with DTD
xvalidate  -dtd ../../samples/data/books.dtd < ../../samples/data/books.xml || echo Validation failed

echo Validating with xsd
xvalidate  -xsd ../../samples/data/books.xsd  ../../samples/data/books.xml || echo Validation failed

echo Skipping validating with rng - requires msv library
echo validate  -rng ../../samples/data/books.rng  ../../samples/data/books.xml || echo Validation failed

echo Following should fail
xvalidate  -xsd ../../samples/data/books.xsd  ../../samples/data/othello.xml  2>/dev/null || echo Successfully trapped invalid schema
