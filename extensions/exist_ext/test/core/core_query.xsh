import module e=../../bin/module.xml
e:query -q <{{
declare variable $x := "hi" ;
<foo>{$x}</foo>
}}>
