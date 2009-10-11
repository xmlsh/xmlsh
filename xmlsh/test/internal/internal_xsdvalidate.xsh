echo Validating with xsd
xsdvalidate ../../samples/data/books.xsd  ../../samples/data/books.xml || echo Validation failed

