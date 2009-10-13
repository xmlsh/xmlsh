# Test a Mysql dump from a local mysql server
# configured with the following
# host: localhost
# db: xmlsh
# user: xmlsh
# password: password

CP=/java/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar

xsql -cp $CP -c jdbc:mysql://localhost/xmlsh -u xmlsh -p password -d org.gjt.mm.mysql.Driver  -q 'select * from books'
