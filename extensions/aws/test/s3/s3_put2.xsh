# Test of s3 CreateBucket

import module aws=aws
import commands posix

#foo has name of bucket just created


bucket=xmlsh-org-test-$RANDOM
aws:s3-create-bucket $bucket
foo=s3://$bucket

lib=$(xfile $PWD/../../lib)

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

X=c:/Work/DEI/xmlsh/extensions/aws/lib
cp $lib/*.jar .


aws:s3put -v -r * $foo/awss3/
aws:s3ls $foo/awss3/ | xdelattribute -a bucket


aws:s3-delete -r $foo/awss3

aws:s3-delete-bucket $bucket

rm -rf $TMPDIR/awss3


exit 0