# time command ...
_T1=<[current-time()]>
"$@"
echo  <[ let $t := current-time() - $_T1
	return (
	hours-from-duration($t),'hours' , 
	minutes-from-duration($t),'minutes',
	seconds-from-duration($t),'seconds' 
)
]> 1>&2
