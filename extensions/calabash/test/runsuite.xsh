# runsuite test-suite.xml
declare namespace t="http://xproc.org/ns/testsuite" 
declare namespace p="http://www.w3.org/ns/xproc"
declare namespace c="http://www.w3.org/ns/xproc-step"
declare namespace err="http://www.w3.org/ns/xproc-error"

_BINDIR=$(xfile -d $0)

xread suite < $1

for t in <[ $suite/t:test-suite/t:test/@href/string() ]> ; do
  case $t in 
  required/*)  $_BINDIR/runtest.xsh $t ;;
  *) : ;;
  esac
  
done


declare namespace "http://xproc.org/ns/testreport"
echo Generating summary in 
cd $_BINDIR/_out
pwd


_results=<[ () ]>

for d in required  ; do
  cd $d 
  echo <[ <title>{$d}</title> ]> > _title.xml
  xcat -w test-suite _title.xml */result.xml > results.xml 
  xread r < results.xml
  _results=<[ $_results , $r ]>
  cd .. 
done

echo <[ 
<test-report>
	<title>xmlsh</title>
	<date>{fn:current-date()}</date>
	<processor>
	<name>xproc2xmlsh</name>
	<episode>1</episode>
	<language>English</language>
	<vendor>xmlsh</vendor>
	<vendor-uri>http://www.xmlsh.org</vendor-uri>
	<psvi-supported>false</psvi-supported>
	<version>0.0.1.3</version>
	<xpath-version>2.0</xpath-version>
	<xproc-version>unknown</xproc-version>
	</processor>
	{ $_results }
</test-report>
]> > xmlsh-test-report.xml

echo Done tests
echo Pass: $(xpath 'count(//pass)' < xmlsh-test-report.xml)
echo Fail: $(xpath 'count(//fail)' < xmlsh-test-report.xml)


	