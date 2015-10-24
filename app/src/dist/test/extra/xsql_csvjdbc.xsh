# Test a csvjdbc dump from a csv file
# connector from http://csvjdbc.sourceforge.net/

CP=$PWD/lib/csvjdbc*.jar

cd ../../samples/data
xsql -cp $CP -d org.relique.jdbc.csv.CsvDriver  -o suppressHeaders=true -c "jdbc:relique:csv:$PWD" "select * from books"

