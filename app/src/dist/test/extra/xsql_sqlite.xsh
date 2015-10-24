# Test a sqlite insert and create and select using jdbc driver for sqlite from 
# http://www.zentus.com/sqlitejdbc/

_TEST=<[
document {
 <test>
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
  </test>

}
]>





CP=$PWD/lib/*sqlite*.jar
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# create sqlite DB 

xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table test (TITLE , AUTHOR , PUBLISHER , `PUB-DATE` , LANGUAGE, PRICE , QUANTITY , ISBN , PAGES , DIMENSIONS , WEIGHT  )' > /dev/null

# Add data
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert <{_TEST} > /dev/null

# query data
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -q 'select * from test'

cd ..
rm -r -f $TMPDIR/_xmlsh
 
