# XML sub processes
files=$<(xls)
echo First file is <[$files//file[1]/@name/string()]>
