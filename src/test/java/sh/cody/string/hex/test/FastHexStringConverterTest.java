package sh.cody.string.hex.test;

import org.junit.jupiter.api.BeforeAll;
import sh.cody.string.hex.FastHexStringConverter;
import sh.cody.string.hex.HexStringConverter;

public class FastHexStringConverterTest extends HexStringConverterTest {
  private static HexStringConverter impl;

  @BeforeAll
  static void initImpl() {
    impl = new FastHexStringConverter();
  }

  @Override
  HexStringConverter getImpl() {
    return impl;
  }
}
