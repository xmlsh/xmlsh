# tricky lexical case from http://www.aosabook.org/en/bash.html
#
for for in for; do 
	for=for; 
done

echo $for
