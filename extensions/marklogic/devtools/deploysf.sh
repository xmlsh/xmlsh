# Deploy a build to sourceforge
rsync -avP -e ssh ../_dist/ daldei@frs.sourceforge.net:uploads/
