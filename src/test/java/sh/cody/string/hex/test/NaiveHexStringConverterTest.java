package sh.cody.string.hex.test;

import org.junit.jupiter.api.BeforeAll;
import sh.cody.string.hex.HexStringConverter;
import sh.cody.string.hex.NaiveHexStringConverter;

public class NaiveHexStringConverterTest extends HexStringConverterTest {
  private static HexStringConverter impl;

  @BeforeAll
  static void initImpl() {
    impl = new NaiveHexStringConverter();
  }

  @Override
  HexStringConverter getImpl() {
    return impl;
  }
}
