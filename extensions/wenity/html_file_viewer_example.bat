@echo off

echo.
echo * HTML file viewer example for Wenity for Windows *
echo.
echo This example assumes that Wenity is installed at c:\wenity
echo.

rem *                           NOTE how the file path is specified
java -jar wenity.jar fileViewer /C:/wenity/doc/wenity_manual.html "Please select a button after you read the text bellow" "Yes,No"