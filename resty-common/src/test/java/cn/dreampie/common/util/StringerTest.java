package cn.dreampie.common.util;

import org.junit.Test;

public class StringerTest {

  @Test
  public void testStringer() {
    System.out.println(Stringer.underlineCase("ISOCertifiedStaff"));
    System.out.println(Stringer.underlineCase("CertifiedStaff"));
    System.out.println(Stringer.underlineCase("UserID"));
    System.out.println(Stringer.camelCase("iso_certified_staff"));
    System.out.println(Stringer.camelCase("certified_staff"));
    System.out.println(Stringer.camelCase("user_id"));
  }
}