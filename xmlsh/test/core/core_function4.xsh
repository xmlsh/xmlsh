# Test of functions with optional returns



function f1() {
	return 
}
function f2() {
	return ;
}

function f3() {
	return $1
}

function f4() {
	return $* ;
}

function f5() {
    echo $0 start
	return ;
	echo $0 end
}
function f6() {
    echo $0 start
	return ;
	echo $0 end
}

function f7() {
    echo $0 start
	return $1 
	echo $0 end
}

function f8() {
    echo $0 start
	return $1 ;
	echo $0 end
}
function f8() {
    echo $0 start
	return $* 
	echo $0 end
}

function f8() {
    echo $0 start
	return $@ ;
	echo $0 end
}	
	  
f1
f2
f3
f4
f5
f6
f7
f8


f1 && f2 && f3 && f4 && echo Passed 
f3 0 && f4 0 && echo Passed 
f3 1 || f4 1 || echo Passed 
x=f8( 1 2 3 )
echo ${#x}
[ ${#x} -eq 3 ] && echo Passed || echo Failed



  