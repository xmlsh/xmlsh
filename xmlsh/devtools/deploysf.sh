# Deploy a build to sourceforge
rsync -avP -e ssh $XMLSH/_dist/ daldei@frs.sourceforge.net:uploads/