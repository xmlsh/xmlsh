# Initialize MarkLogic connection properties
# This user must have full access to a TEST database which is OK to fully delete for purposes of this test.
# WARNING : DO NOT SET THIS TO A LIVE DATABASE YOU WILL LOSE EVERYTHING

MLCONNECT="xcc://test:test@home:8020"
MLCONNECT_SSL="xccs://test:test@home:8021"
ML_ROLE="test"
import module ml=marklogic