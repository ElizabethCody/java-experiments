package sh.cody.string.interpolate.test;

import org.junit.jupiter.api.Test;
import sh.cody.string.interpolate.Context;
import sh.cody.string.interpolate.ContextualizedStringInterpolator;
import sh.cody.string.interpolate.ShellStyleStringInterpolator;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ShellStyleStringInterpolatorTest {
  final static Context mockContext = Map.of(
    "A", "value a",
    "_", "value _",
    "helloWorld", "Hello, World!",
    "ONE", "1",
    "TWO", "2",
    "ZERO", "0",
    "", "null",
    "Function::identity", "identity function"
  )::get;

  final static ContextualizedStringInterpolator interpolator =
    new ShellStyleStringInterpolator().forContext(mockContext);

  @Test
  void testMatches() {
    assertEquals("Hello, World!", interpolator.interpolate("${helloWorld}"));
    assertEquals("Hello, World!", interpolator.interpolate("%helloWorld%"));
    assertEquals("0 -> 1 -> 2", interpolator.interpolate("${ZERO} -> %ONE% -> %TWO%"));
    assertEquals("120% value a", interpolator.interpolate("%ONE%%TWO%%ZERO%%% ${A}"));
  }

  @Test
  void testMisses() {
    assertEquals("${TWELVE}", interpolator.interpolate("${TWELVE}"));
    assertEquals("%TWELVE%", interpolator.interpolate("%TWELVE%"));
    assertEquals("Value: ${TWELVE}", interpolator.interpolate("Value: ${TWELVE}"));
  }

  @Test
  void testMissesWithDefault() {
    assertEquals("12", interpolator.interpolate("${TWELVE:12}"));
    assertEquals("", interpolator.interpolate("${EMPTY:}"));
  }

  @Test
  void testIncomplete() {
    assertEquals("% 0 1 2 %THREE% %FOURnull{}{}", interpolator.interpolate("%% %ZERO% ${ONE} ${TWO} %THREE% %FOUR${}{}{}"));
    assertEquals("Hello, World! %ZERO 0", interpolator.interpolate("%helloWorld% %ZERO ${ZERO}"));
    assertEquals("a %helloWorld", interpolator.interpolate("a %helloWorld"));
    assertEquals("a ${helloWorld", interpolator.interpolate("a ${helloWorld"));
    assertEquals("a ${TWELVE:12", interpolator.interpolate("a ${TWELVE:12"));
    assertEquals("a ${EMPTY:", interpolator.interpolate("a ${EMPTY:"));
    assertEquals("a ${", interpolator.interpolate("a ${"));
    assertEquals("a $", interpolator.interpolate("a $"));
  }

  @Test
  void testShWhitespace() {
    assertEquals("Hello, World!", interpolator.interpolate("${ helloWorld }"));
  }

  @Test
  void testDosless() {
    final ContextualizedStringInterpolator interpolator = new ShellStyleStringInterpolator(true, false).forContext(mockContext);
    assertEquals("%ZERO% 0", interpolator.interpolate("%ZERO% ${ZERO}"));
  }

  @Test
  void testShless() {
    final ContextualizedStringInterpolator interpolator = new ShellStyleStringInterpolator(false, false, true).forContext(mockContext);
    assertEquals("0 ${ZERO}", interpolator.interpolate("%ZERO% ${ZERO}"));
  }

  @Test
  void testDefaultlessSh() {
    final ContextualizedStringInterpolator interpolator = new ShellStyleStringInterpolator(false).forContext(mockContext);
    assertEquals("identity function", interpolator.interpolate("${Function::identity}"));
  }
}
