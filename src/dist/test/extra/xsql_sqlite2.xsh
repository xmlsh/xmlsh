# Test a sqlite insert and create and select using jdbc driver for sqlite from 
# http://www.zentus.com/sqlitejdbc/

xread _TEST1 <<EOF
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
EOF

xread _TEST2 <<EOF
      <class>
            <ROW>
                  <CLASS_ID><![CDATA[1]]></CLASS_ID>
                  <CLASS_NAME><![CDATA[My Test Class]]></CLASS_NAME>
            </ROW>
      </class>
EOF

xread _TEST3 <<EOF
     <unknown>
            <ROW  DRUG_ID="2" SUB_CLASS_ID="1" />
      </unknown>

EOF



# test imported library into global classloader
import java lib/*sqlite*.jar

[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# create sqlite DB 

xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table dosing (Dosing_ID , Drug_ID , Drug_Name , Generic_Name , Adult_Dose_String, Peds_Dose_String , Dosage_Form , Strength ,Dose_Units ,  Route)' > /dev/null
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table class (CLASS_ID , CLASS_NAME )' > /dev/null
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table drug_class_indexed (DRUG_ID , SUB_CLASS_ID )' > /dev/null

# Add data TEST1
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert -tableAttr name -fieldAttr NAME <{_TEST1}> /dev/null
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert <{_TEST2}> /dev/null
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert -table drug_class_indexed -attr <{_TEST3}> /dev/null

# query data

_OUT=()
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -root dosing -q 'select * from dosing' >{_OUT}
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -root class -q 'select * from class' >>{_OUT}
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -root drug_class_indexed -attr -q 'select * from drug_class_indexed' >>{_OUT}
xecho <[ <all>{$_OUT}</all> ]>

cd ..
rm -r -f $TMPDIR/_xmlsh
#echo DB is $TMPDIR/_xmlsh/sqlite.db

 
