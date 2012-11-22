# Test of s3 CreateBucket

import module aws=aws

#foo has name of bucket just created

foo=$(aws:s3CreateBucket -r random-)

#verify that new bucket exists

aws:s3ls | xpath -q '//bucket[@name eq concat($bar, "")]' -v bar $foo > /dev/null

if [ $? -eq 0 ] ; then
	echo success
    aws:s3DeleteBucket $foo
    exit 0
fi

aws:s3DeleteBucket $foo

exit 1