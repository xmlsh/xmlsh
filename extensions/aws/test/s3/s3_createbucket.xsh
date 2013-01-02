# Test of s3 CreateBucket

import module aws=aws

#foo has name of bucket just created

bucket=xmlsh-org-test-$RANDOM
aws:s3-create-bucket $bucket

#verify that new bucket exists

aws:s3ls | xpath -q '//bucket[@name eq concat($bar, "")]' -v bar $bucket > /dev/null

if [ $? -eq 0 ] ; then
	echo success
    aws:s3-delete-bucket $bucket
    exit 0
fi

aws:s3-delete-bucket $bucket

exit 1