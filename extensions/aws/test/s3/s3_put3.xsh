# Test of simple s3put file dest

import module aws=aws
import commands posix

#foo has name of bucket just created
bucket=xmlsh-org-test-$RANDOM
aws:s3-create-bucket $bucket
foo=s3://$bucket/subdir

rm -rf $TMPDIR/awss3
mkdir $TMPDIR/awss3
cd $TMPDIR/awss3

# make some files

cat > f1 <<EOF
This is f1
EOF

# step 1 - verify single file upload

aws:s3put f1 $foo/f1
aws:s3get $foo/f1 f1back


xcmp f1 f1back && echo Step 1 Success compared Success

aws:s3Delete $foo/f1

rm -f f1back


aws:s3-delete -r $foo/awss3

aws:s3-delete-bucket $bucket
rm -rf $TMPDIR/awss3


exit 0