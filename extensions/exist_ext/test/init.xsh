# Initialize MarkLogic connection properties
# This user must have full access to a TEST database which is OK to fully delete for purposes of this test.
# WARNING : DO NOT SET THIS TO A LIVE DATABASE YOU WILL LOSE EVERYTHING

EXIST_CONNECT="http://home:8080/exist/rest"
import module e=exist
_TESTDIR=/db/_test