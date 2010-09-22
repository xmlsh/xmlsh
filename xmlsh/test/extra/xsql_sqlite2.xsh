# Test a sqlite insert and create and select using jdbc driver for sqlite from 
# http://www.zentus.com/sqlitejdbc/

xread _TEST <<EOF
<RESULTS entrytag="ROW" fieldtag="COLUMN">      
      <table name="dosing">
            <ROW>
                  <COLUMN NAME="Dosing_ID"><![CDATA[1]]></COLUMN>
                  <COLUMN NAME="Drug_ID"><![CDATA[2]]></COLUMN>
                  <COLUMN NAME="Drug_Name"><![CDATA[3]]></COLUMN>
                  <COLUMN NAME="Generic_Name"><![CDATA[4]]></COLUMN>
                  <COLUMN NAME="Adult_Dose_String"><![CDATA[5]]></COLUMN>
                  <COLUMN NAME="Peds_Dose_String"><![CDATA[6]]></COLUMN>
                  <COLUMN NAME="Dosage_Form"><![CDATA[7]]></COLUMN>
                  <COLUMN NAME="Strength"><![CDATA[8]]></COLUMN>
                  <COLUMN NAME="Dose_Units"><![CDATA[9]]></COLUMN>
                  <COLUMN NAME="Route"><![CDATA[10]]></COLUMN>          
            </ROW>
      </table>
      <table name="class">
            <ROW>
                  <COLUMN NAME="CLASS_ID"><![CDATA[1]]></COLUMN>
                  <COLUMN NAME="CLASS_NAME"><![CDATA[My Test Class]]></COLUMN>
            </ROW>
      </table>
      <table name="drug_class_indexed">
            <ROW>
                  <COLUMN NAME="DRUG_ID"><![CDATA[2]]></COLUMN>
                  <COLUMN NAME="SUB_CLASS_ID"><![CDATA[1]]></COLUMN>
            </ROW>
      </table>
</RESULTS>
EOF




CP=/java/sqlitejdbc/sqlitejdbc-v056.jar
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# create sqlite DB 

xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table dosing (Dosing_ID , Drug_ID , Drug_Name , Generic_Name , Adult_Dose_String, Peds_Dose_String , Dosage_Form , Strength ,Dose_Units ,  Route)' > /dev/null
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table class (CLASS_ID , CLASS_NAME )' > /dev/null
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table drug_class_indexed (DRUG_ID , SUB_CLASS_ID )' > /dev/null

# Add data
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert -tableAttr name -fieldAttr NAME <{_TEST}> /dev/null

# query data
xsql -cp $CP -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -q 'select * from dosing'

cd ..
rm -r -f $TMPDIR/_xmlsh
#echo DB is $TMPDIR/_xmlsh/sqlite.db

 