#!/bin/bash

echo
echo "Bash examples for Wenity v1.5"
echo 


## --------------------------------
## Ask user to select a button
## --------------------------------
java -jar wenity.jar dialog question "Please select|a button bellow:" "Yes,No,Perhaps"
RESULT=$?
case "$RESULT" in
    1)  echo "User selected YES."
        ;;

    2)  echo "User selected NO."
        ;;

    3)  echo "User selected PERHAPS."
        ;;

    254) echo "Dialog is cancelled by user."
        ;;

    255) echo "An error occurred."
        ;;
esac


## --------------------------------
## Ask user to select a file
## --------------------------------
# clean up previous response file if any
rm -f wenity_response.txt

java -jar wenity.jar fileSelector "Please select an existing text or pdf file" "txt,pdf" "File should exist"
RESULT=$?
if [ $RESULT -eq 0 ]; then
    SELECTED_FILE=`cat wenity_response.txt`
    echo "User selected: $SELECTED_FILE"
    # clean up response file
    rm wenity_response.txt
else
    # this can also be an error, but that check is omitted
    echo "File selector is cancelled by user."
fi


## --------------------------------
## Ask user to input something
## --------------------------------
# clean up previous response file if any
rm -f wenity_response.txt

java -jar wenity.jar dialog input "Please input your details:"
RESULT=$?
if [ $RESULT -eq 0 ]; then
    USER_INPUT=`cat wenity_response.txt`
    echo "User typed: $USER_INPUT"
    # clean up response file
    rm wenity_response.txt
else
    # this can also be an error, but that check is omitted
    echo "Input dialog is cancelled by user."
fi


## -----------------------------------
## Ask user to fill in a custom form
## -----------------------------------
# clean up previous response file if any
rm -f wenity_response.txt

java -jar wenity.jar dialog custom custom_dlg_example.ddf
RESULT=$?
if [ $RESULT -eq 0 ]; then
    echo "User data:"
    SEL_BUTTON=`head -1 wenity_response.txt`
    FIRST_NAME=`head -2 wenity_response.txt | tail -1`
    LAST_NAME=`head -3 wenity_response.txt | tail -1`
    COUNTRY=`head -4 wenity_response.txt | tail -1`
    echo "--> User name is $FIRST_NAME $LAST_NAME from $COUNTRY. Selected button number is $SEL_BUTTON."
    # clean up response file
    rm wenity_response.txt
else
    # this can also be an error, but that check is omitted
    echo "Form is cancelled by user."
fi


## --------------------------------
## Show a file to the user
## --------------------------------
java -jar wenity.jar fileViewer `pwd`/doc/wenity_manual.html "Please select a button after you read the text bellow" "Yes,No"
RESULT=$?
case "$RESULT" in
    1)  echo "User selected YES."
        ;;

    2)  echo "User selected NO."
        ;;

    254) echo "File viewer is cancelled by user."
        ;;

    255) echo "An error occurred. File not found?"
        ;;
esac


## --------------------------------
## Check if file exists
## --------------------------------
java -jar wenity.jar fileSystem pathExists /bin/ls
RESULT=$?
case "$RESULT" in
    1)  echo "The /bin/ls file exists."
        ;;

    2)  echo "The /bin/ls file does not exist."
        ;;

    255) echo "An error occurred."
        ;;
esac


## ----------------------------------------------------
## Check if current partition has 100 Mb of free space
## ----------------------------------------------------
java -jar wenity.jar fileSystem hasFreeSpace . 100
RESULT=$?
case "$RESULT" in
    1)  echo "Current partition has at least 100 Mb free space."
        ;;

    2)  echo "Current partition has less than 100 Mb free space."
        ;;

    255) echo "An error occurred."
        ;;
esac


## ----------------------------------------------------
## Show progress indicator
## ----------------------------------------------------
rm -rf wenity_status
java -jar wenity.jar progressIndicator wenity_status "Please wait until the operation completes (approx. 3 seconds)" &
sleep 3
echo "Creating status file to finish progress indicator"
touch wenity_status
sleep 2
rm -rf wenity_status


## ----------------------------------------------------
## Show a notification balloon
## ----------------------------------------------------
echo "Showing notification balloon"
java -jar wenity.jar notifier info "Wenity demo" "This is the end of the Wenity demonstration script" 3

echo