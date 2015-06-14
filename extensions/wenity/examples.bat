@echo off

echo.
echo * Windows XP/Vista/7 examples for Wenity v1.5 *
echo.

SETLOCAL EnableDelayedExpansion

set|findstr "JAVA" > nul
if ERRORLEVEL 1 (
    echo Java not found. Please install Java.
    goto :eof
)


rem ------------------------------------------
rem     Ask user to select a button
rem ------------------------------------------

java -jar wenity.jar dialog question "Please select|a button bellow:" "Yes,No,Perhaps"
goto RESULT_%ERRORLEVEL%

rem this should never be seen
goto :eof

:RESULT_1
    echo User selected YES.
    goto :demo_2

:RESULT_2
    echo User selected NO.
    goto :demo_2

:RESULT_3
    echo User selected PERHAPS.
    goto :demo_2

:RESULT_254
    echo Dialog is cancelled by user.
    goto :demo_2

:RESULT_255
    echo An error occurred.
    goto :demo_2


:demo_2


rem ------------------------------------------
rem     Ask user to select a file
rem ------------------------------------------

rem clean up previous response file if any
del wenity_response.txt 2>nul

java -jar wenity.jar fileSelector "Please select an existing text or pdf file" "txt,pdf" "File should exist"
if %ERRORLEVEL%==0 (
    set /p SELECTED_FILE=<wenity_response.txt
    echo User selected !SELECTED_FILE!
    rem clean up response file
    del wenity_response.txt
) else (
    rem this can also be an error, but that check is omitted
    echo File selector is cancelled by user.
)


rem ------------------------------------------
rem     Ask user to input something
rem ------------------------------------------

rem clean up previous response file if any
del wenity_response.txt 2>nul

java -jar wenity.jar dialog input "Please input your details:"
if %ERRORLEVEL%==0 (
    set /p USER_INPUT=<wenity_response.txt
    echo User typed: !USER_INPUT!
    rem clean up response file
    del wenity_response.txt
) else (
    rem this can also be an error, but that check is omitted
    echo Input dialog is cancelled by user.
)


rem ------------------------------------------
rem     Show a file to the user
rem ------------------------------------------

java -jar wenity.jar fileViewer examples.sh "Please select a button after you read the text bellow" "Yes,No"
goto RESULT_A_%ERRORLEVEL%
goto :eof

:RESULT_A_1
    echo User selected YES.
    goto :demo_3

:RESULT_A_2
    echo User selected NO.
    goto :demo_3

:RESULT_A_254
    echo File viewer is cancelled by user.
    goto :demo_3

:RESULT_A_255
    echo An error occurred. File not found?
    goto :demo_3

:demo_3


rem --------------------------------
rem Check if file exists
rem --------------------------------

java -jar wenity.jar fileSystem pathExists \Windows\win.ini
if %ERRORLEVEL%==1 (
    echo File "\Windows\win.ini" exists
) else (
    rem this can also be an error, but that check is omitted
    echo File "\Windows\win.ini" does not exist
)


rem ----------------------------------------------------
rem Check if current partition has 100 Mb of free space
rem ----------------------------------------------------

java -jar wenity.jar fileSystem hasFreeSpace . 100
if %ERRORLEVEL%==1 (
    echo Current partition has at least 100 Mb free space.
) else (
    rem this can also be an error, but that check is omitted
    echo Current partition has less than 100 Mb free space.
)


rem ----------------------------------------------------
rem Show a notification balloon
rem ----------------------------------------------------

echo Showing notification balloon
java -jar wenity.jar notifier info "Wenity demo" "This is the end of the Wenity demonstration script" 3

echo.
pause