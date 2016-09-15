package org.xmlsh.aws.util;


import static org.hamcrest.core.IsEqual.*;

import static org.junit.Assert.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class S3PathTest {


  @Test
  public void testIsDirectory() throws Exception {
    S3Path s3Path = new S3Path("bucket","key");
    assertFalse( s3Path.isDirectory());
  }
  @Test
  public void testAppendPath() throws Exception {
    S3Path p = new S3Path("bucket","key");
    p.appendPath("subkey");
    assertThat("key/subkey",equalTo(p.getKey()));

  }

}
