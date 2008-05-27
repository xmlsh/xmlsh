# core_brace.xsh

# Non-outputing test
{ false; }
{ false ; } && { echo invalid return ; exit 1 ; }
{ false ; true ; }


# multi line

{ 
	true ; 
	false ;
}

# multi line with if

true && {
	true ; 
	false ;
}

# simple output

echo {  not reserved  middle or end of command }


{ echo true ;  }
{ echo line1 ; echo line2 ; }

# xml piping
{ echo '<foo/>' ; } | xcat

C=echo
${C}
echo '<foo2/>' | { xcat | xcat;}




# { env does change
A=1
{ A=2  }

if [ ! $A = 2 ] ; then 
	echo  Brace environment wrong A=$A
fi



exit 0

