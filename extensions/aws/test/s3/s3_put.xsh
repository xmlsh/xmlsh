# Test of s3 CreateBucket

import module aws=aws
import commands posix

#foo has name of bucket just created
bucket=xmlsh-org-test-$RANDOM
aws:s3-create-bucket $bucket
foo=s3://$bucket

rm -rf $TMPDIR/awss3
mkdir $TMPDIR/awss3
cd $TMPDIR/awss3

# make some files

cat > f1 <<EOF
This is f1
EOF

cat > f2 <<EOF
This is f2
EOF

cat > f3 <<EOF
This is f3
EOF

# step 1 - verify single file upload

aws:s3put f1 $foo
aws:s3get $foo/f1 f1back

xcmp f1 f1back && echo Step 1 Success compared Success

aws:s3Delete $foo/f1

rm -f f1back

# step 2 - verify multiple single file upload

aws:s3put f1 f2 $foo
aws:s3get $foo/f1 f1back
aws:s3get $foo/f2 f2back

xcmp f1 f1back && echo Step 2-f1 Success compared Success
xcmp f2 f2back && echo Step 2-f2 Success compared Success

aws:s3-delete $foo/f1
aws:s3-delete $foo/f2
rm -f f1back
rm -f f2back

# step 3 - verify multifile directory upload

cd ..

aws:s3put -r awss3 $foo

cd awss3

aws:s3get $foo/awss3/f1 f1back
aws:s3get $foo/awss3/f2 f2back
aws:s3get $foo/awss3/f3 f3back

xcmp f1 f1back && echo Step 3-f1 Success compared Success
xcmp f2 f2back && echo Step 3-f2 Success compared Success
xcmp f3 f3back && echo Step 3-f3 Success compared Success

aws:s3-delete -r $foo/awss3

aws:s3-delete-bucket $bucket
rm -rf $TMPDIR/awss3

exit 0