words="catch for if elif then else fi do case while until try esac finally return done in function local"
for w in $words ; do echo $w ; done

echo $words
echo catch for if elif then else fi do case while until try esac finally return done in function local
echo catch= for= catch, fi, if, =if if= :if if: 
echo '[correct] the {} will add an extra empty argument'
echo if ,if if, ,if, do ,,do =do =:do done ,done done{} {done}
set if ,if if, ,if, do ,,do =do =:do done ,done done{} {done}
echo Should be 13: $#
echo '[correct] the {} will vanish on arg expansion'
echo $*
echo $@
echo TODO: conversion from sequence to array or string - any way to preserve
echo "$@"
echo ${@} 
echo {$@}
echo {$*}
set "$@" 
echo $#