# Test a Mysql dump from a local mysql server
# configured with the following
# host: localhost
# db: xmlsh
# user: xmlsh
# password: password

_DDL=<{{
CREATE TABLE `books` (
  `TITLE` varchar(255) NOT NULL,
  `AUTHOR` varchar(255) DEFAULT NULL,
  `PUBLISHER` varchar(255) DEFAULT NULL,
  `PUB-DATE` date DEFAULT NULL,
  `LANGUAGE` varchar(255) DEFAULT NULL,
  `PRICE` decimal(5,2) DEFAULT NULL,
  `QUANTITY` int(11) DEFAULT NULL,
  `ISBN` varchar(255) DEFAULT NULL,
  `PAGES` int(11) DEFAULT NULL,
  `DIMENSIONS` varchar(255) DEFAULT NULL,
  `WEIGHT` decimal(5,2) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
}}>


_TEST=<[
document {
 <books>
  <row>
    <TITLE>Pride and Prejudice</TITLE>
    <AUTHOR>Jane Austen</AUTHOR>
    <PUBLISHER>Modern Library</PUBLISHER>
    <PUB-DATE>2002-12-31</PUB-DATE>
    <LANGUAGE>English</LANGUAGE>
    <PRICE>4.95</PRICE>
    <QUANTITY>187</QUANTITY>
    <ISBN>679601686</ISBN>
    <PAGES>352</PAGES>
    <DIMENSIONS>8.3 5.7 1.1</DIMENSIONS>
    <WEIGHT>6.10</WEIGHT>
  </row>
  <row>
    <TITLE>Wuthering Heights</TITLE>
    <AUTHOR>Charlotte Bront</AUTHOR>
    <PUBLISHER>Penguin Classics</PUBLISHER>
    <PUB-DATE>2002-12-31</PUB-DATE>
    <LANGUAGE>English</LANGUAGE>
    <PRICE>6.58</PRICE>
    <QUANTITY>113</QUANTITY>
    <ISBN>141439556</ISBN>
    <PAGES>430</PAGES>
    <DIMENSIONS>1.0 5.2 7.8</DIMENSIONS>
    <WEIGHT>11.20</WEIGHT>
  </row>
  </books>
}
]>





CP=/java/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar

# just open the connection
set -e
xsql -cp $CP -c jdbc:mysql://localhost/xmlsh -u xmlsh -p password -d org.gjt.mm.mysql.Driver -cache >/dev/null


xsql -cache -execute 'DROP table books' >/dev/null
xsql -cache -execute {$_DDL} >/dev/null
# Add data
xsql -cache -insert <{_TEST} >/dev/null


xsql -cache -q 'select * from books'
xsql -cache -close >/dev/null
