# ls command - only handle no-option case

xls "$@" | xpath '//file/@name/string()'