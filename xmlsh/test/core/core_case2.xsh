# core_case2.xsh
# Test of case
i="delete"
for i in update insert delete ; do 
	echo $i 
	case $i in
	   update|insert) echo update/insert ;;
	   delete) echo delete ;;
	esac
done

