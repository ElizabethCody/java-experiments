package sh.cody.string.hex.test;

import org.junit.jupiter.api.BeforeAll;
import sh.cody.string.hex.FasterHexStringConverter;
import sh.cody.string.hex.HexStringConverter;

public class FasterHexStringConverterTest extends HexStringConverterTest {
  private static HexStringConverter impl;

  @BeforeAll
  static void initImpl() {
    impl = new FasterHexStringConverter();
  }

  @Override
  HexStringConverter getImpl() {
    return impl;
  }
}
