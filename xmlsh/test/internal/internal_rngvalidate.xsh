# test of rngvalidate

echo RNG Validation
rngvalidate ../../samples/data/books.rng ../../samples/data/books.xml || { echo failed RNG validation ; exit 1 ; }

