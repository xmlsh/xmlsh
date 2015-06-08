# test of xls command
import commands posix
TD=$TMPDIR/_xmlsh
rm -rf $TD
mkdir $TD
cd $TD

echo > file1.dat
echo -n fixedlen > file2.dat
echo > file3.dat

D=$<(xls)


[ <[count($D//file)]> -ne 3 ] && echo FAILED File count should be 3 

[ <[$D/dir/file[2]/@name/string()]> = file2.dat ] || echo FAILED expected file2.dat

D2=$<(xls -l)
[ <[ xs:integer($D2/dir/file[2]/@length) ]> -eq 8 ] || echo FAILED expected length of file2.dat = 8

cd ..
rm -rf $TD

