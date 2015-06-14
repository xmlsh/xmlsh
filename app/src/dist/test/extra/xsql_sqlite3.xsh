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
            <ROW>
                  <COLUMN NAME="Dosing_ID">2</COLUMN>
                  <COLUMN NAME="Drug_ID">3</COLUMN>
                  <COLUMN NAME="Drug_Name">Name</COLUMN>
                  <COLUMN NAME="Generic_Name">Generic Name</COLUMN>
                  <COLUMN NAME="Adult_Dose_String">Adult Dose</COLUMN>
                  <COLUMN NAME="Peds_Dose_String">Peds Dose</COLUMN>
                  <COLUMN NAME="Dosage_Form">Dosage Form</COLUMN>
                  <COLUMN NAME="Strength">Strength</COLUMN>
                  <COLUMN NAME="Dose_Units">Units</COLUMN>
                  <COLUMN NAME="Route">Route</COLUMN>          
            </ROW>
             <ROW>
                  <COLUMN NAME="Dosing_ID">3</COLUMN>
                  <COLUMN NAME="Drug_ID">3</COLUMN>
                  <COLUMN NAME="Drug_Name">Name</COLUMN>
                  <COLUMN NAME="Generic_Name">Generic Name</COLUMN>
                  <COLUMN NAME="Adult_Dose_String">Adult Dose</COLUMN>
                  <COLUMN NAME="Peds_Dose_String">Peds Dose</COLUMN>
                  <COLUMN NAME="Dosage_Form">Dosage Form</COLUMN>
                  <COLUMN NAME="Strength">Strength</COLUMN>
                  <COLUMN NAME="Dose_Units">Units</COLUMN>
                  <COLUMN NAME="Route">Route</COLUMN>          
            </ROW>


      </table>
EOF





import java /java/sqlitejdbc/*.jar

[ -d $TMPDIR/_xmlsh ] && rm -rf $TMPDIR/_xmlsh
mkdir $TMPDIR/_xmlsh
cd $TMPDIR/_xmlsh

# create sqlite DB 

xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -execute 'CREATE table dosing (Dosing_ID INTEGER PRIMARY KEY , Drug_ID , Drug_Name , Generic_Name , Adult_Dose_String, Peds_Dose_String , Dosage_Form , Strength ,Dose_Units ,  Route)' > /dev/null

# Add data TEST1
xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -insert -tableAttr name -fieldAttr NAME <{_TEST1}

# query data

#_OUT=()
#xsql -c jdbc:sqlite:sqlite.db -d org.sqlite.JDBC -root dosing -q 'select * from dosing' >{_OUT}
#xecho <[ <all>{$_OUT}</all> ]>

cd ..
rm -r -f $TMPDIR/_xmlsh
#echo DB is $TMPDIR/_xmlsh/sqlite.db

 