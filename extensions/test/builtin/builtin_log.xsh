# Test log feature
# Note that this is not easily testable so just run several variants 
# and make sure they dont fail

# Exit on faiure
set -e	
log "This is a log line"
log -c test.class -priority debug "This is a debug line logged to test.class"
echo success
