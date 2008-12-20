# Demo app program
# This is a simple config file driven menu based app display program
# Designed to demonstrate xmlsh features interactively
#
# A menu of commands is printed.  When one is selected
# Then the contents of the command are displayed, then executed
#

# Set global data directory
DEMODIR=$(xfile -d $0)
XROOT=$(xfile -c $DEMODIR/..)
#
# Set absolute path for this script
demo=$(xfile -a $0)
#
# Read menu
xread menu < menu.xml || { echo menu.xml not found ; exit 1 ; }
cmds=<[ $menu/menu/command ]>
#
cmd=""
while true ; do
# Print menu
	echo Demo Examples for xmlsh
	echo =======================
	i=<[1]>
	for cmd in $cmds ; do
    		echo "$i) " <[$cmd/@title/string()]> 
		i=<[$i+1]> 
	done 
	echo =======================
        echo "ENTER to return"

# Read input
	echo -n "Command: " 
	read key || break 
	[ -z "$key" -o "$key" = "q" -o "$key" = "Q"  ] && exit  ;
        [ $key -ge $i ] && continue ;

# Find command
        cmd=<[ $cmds[xs:integer($key)] ]>
        [ <[$cmd]> ] || continue ;

        dir=<[$cmd/@dir/string()]> 
        if [ $dir ] ; then 
# If directory then cd and run menu
            ( cd $dir && $demo )
        else 
# Otherwise print command file then execute
		c=<[ $cmd/@command/string() ]>
                echo ======= $c =======
		cat $c
                echo ======= Press ENTER to run ======
                read x
                ./$c
                echo =======
                echo -n Press ENTER to continue:
                read x
        fi
done

