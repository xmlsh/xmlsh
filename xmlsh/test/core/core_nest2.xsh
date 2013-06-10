


# List of states
# "" or _init      -  initial state with nothing setup except mdb (from init-node)
# init-license-key - License key has been installed
# init-security    - Master created DB or Cluster Joined
# initialized      - Validated that local host is attached to a cluster or locally completed initializaiotn


while true ; do
   case "$_state" in 
     _init)
          echo  message "Initialization of license key successful"
       ;;
              
      init-license-key)
          echo mdb-update-state init-security 
      ;; 

     init-security)
       echo mdb-update-state initialized
       ;;
     initialized)
       echo "Node is initialized"
      ;;
     *) ;;
   esac    
done

