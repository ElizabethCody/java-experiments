package sh.cody.string.hex.test;

import org.junit.jupiter.api.BeforeAll;
import sh.cody.string.hex.FastererHexStringConverter;
import sh.cody.string.hex.HexStringConverter;

public class FastererHexStringConverterTest extends HexStringConverterTest {
  private static HexStringConverter impl;

  @BeforeAll
  static void initImpl() {
    impl = new FastererHexStringConverter();
  }

  @Override
  HexStringConverter getImpl() {
    return impl;
  }
}
