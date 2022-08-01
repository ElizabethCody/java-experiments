package sh.cody.string.hex.test;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import sh.cody.string.hex.HexStringConverter;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HexStringConverterTest {
  abstract HexStringConverter getImpl();

  // ==========================================================================
  // hexStringToBytes() tests
  // ==========================================================================

  @Test
  @DisplayName("hexStringToBytes() with empty string")
  void testHexStringToBytes_Empty() {
    assertArrayEquals(new byte[0], getImpl().fromString(""));
  }

  @Test
  @DisplayName("hexStringToBytes() with '00' octets")
  void testHexStringToBytes_00() {
    final byte[]
      b1 = getImpl().fromString("00"),
      b2 = getImpl().fromString("0000"),
      b3 = getImpl().fromString("000000"),
      b4 = getImpl().fromString("00000000");

    assertArrayEquals(new byte[] {0}, b1);
    assertArrayEquals(new byte[] {0, 0}, b2);
    assertArrayEquals(new byte[] {0, 0, 0}, b3);
    assertArrayEquals(new byte[] {0, 0, 0, 0}, b4);
  }

  @Test
  @DisplayName("hexStringToBytes() with 'FF' octets")
  void testHexStringToBytes_FF() {
    final byte[]
      b1 = getImpl().fromString("FF"),
      b2 = getImpl().fromString("FFFF"),
      b3 = getImpl().fromString("FFFFFF"),
      b4 = getImpl().fromString("FFFFFFFF");

    assertArrayEquals(
      new byte[] {(byte) 0xff}, b1
    );
    assertArrayEquals(
      new byte[] {(byte) 0xff, (byte) 0xff}, b2
    );
    assertArrayEquals(
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff}, b3
    );
    assertArrayEquals(
      new byte[] {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff}, b4
    );
  }

  @Test
  @DisplayName("hexStringToBytes() with invalid whitespace")
  void testHexStringToBytes_InvalidWhitespace() {
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString(" ");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString(" 0f0bac01f");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("00f0ba 01f");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("00f0bac01 ");
    });
  }

  @Test
  @DisplayName("hexStringToBytes() with invalid length")
  void testHexStringToBytes_InvalidLength() {
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("0");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("000");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("00000");
    });
    assertThrows(NumberFormatException.class, () -> {
      getImpl().fromString("0000000");
    });
  }

  // ==========================================================================
  // bytesToHexString() tests
  // ==========================================================================
  @Test
  @DisplayName("bytesToHexString() with empty array")
  void testBytesToHexString_Empty() {
    assertEquals("", getImpl().fromBytes(new byte[0]));
    assertEquals("", getImpl().fromBytes(new byte[0], false));
    assertEquals("", getImpl().fromBytes(new byte[0], true));
  }

  @Test
  @DisplayName("bytesToHexString() with 0x00 bytes")
  void testBytesToHexString_00() {
    final byte[]
      b1 = {(byte) 0x00},
      b2 = {(byte) 0x00, (byte) 0x00},
      b3 = {(byte) 0x00, (byte) 0x00, (byte) 0x00},
      b4 = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00};
    
    assertEquals("00", getImpl().fromBytes(b1));
    assertEquals("00", getImpl().fromBytes(b1, false));
    assertEquals("00", getImpl().fromBytes(b1, true));
    assertEquals("0000", getImpl().fromBytes(b2));
    assertEquals("0000", getImpl().fromBytes(b2, false));
    assertEquals("0000", getImpl().fromBytes(b2, true));
    assertEquals("000000", getImpl().fromBytes(b3));
    assertEquals("000000", getImpl().fromBytes(b3, false));
    assertEquals("000000", getImpl().fromBytes(b3, true));
    assertEquals("00000000", getImpl().fromBytes(b4));
    assertEquals("00000000", getImpl().fromBytes(b4, false));
    assertEquals("00000000", getImpl().fromBytes(b4, true));
  }

  @Test
  @DisplayName("bytesToHexString() with 0xFF bytes")
  void testBytesToHexString_FF() {
    final byte[]
      b1 = {(byte) 0xff},
      b2 = {(byte) 0xff, (byte) 0xff},
      b3 = {(byte) 0xff, (byte) 0xff, (byte) 0xff},
      b4 = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};

    assertEquals("ff", getImpl().fromBytes(b1));
    assertEquals("ff", getImpl().fromBytes(b1, false));
    assertEquals("FF", getImpl().fromBytes(b1, true));
    assertEquals("ffff", getImpl().fromBytes(b2));
    assertEquals("ffff", getImpl().fromBytes(b2, false));
    assertEquals("FFFF", getImpl().fromBytes(b2, true));
    assertEquals("ffffff", getImpl().fromBytes(b3));
    assertEquals("ffffff", getImpl().fromBytes(b3, false));
    assertEquals("FFFFFF", getImpl().fromBytes(b3, true));
    assertEquals("ffffffff", getImpl().fromBytes(b4));
    assertEquals("ffffffff", getImpl().fromBytes(b4, false));
    assertEquals("FFFFFFFF", getImpl().fromBytes(b4, true));
  }

  @Test
  @DisplayName("bytesToHexString() assumes lowercase")
  void testBytesToHexString_AssumesLowercase() {
    final byte[] alphaHexDigits = new byte[] {
      (byte) 0xab, (byte) 0xcd, (byte) 0xef
    };

    assertEquals("abcdef", getImpl().fromBytes(alphaHexDigits));
    assertNotEquals("ABCDEF", getImpl().fromBytes(alphaHexDigits));
  }

  // ==========================================================================
  // hexStringToBytes() & bytesToHexString() composition tests
  // ==========================================================================

  @Test
  @DisplayName(
    "Test bytesToHexString(hexStringToBytes(\"00ffdeadbeefc0ffee\"))"
  )
  void testRoundTripHexString_String() {
    final String initial = "00ffdeadbeefc0ffee";

    final byte[] array = getImpl().fromString(initial);
    final String str = getImpl().fromBytes(array);

    assertEquals(initial, str);
  }

  @Test
  @DisplayName(
    "Test hexStringToBytes(bytesToHexString(0x00ffdeadbeefc0ffee))"
  )
  void testRoundTripHexString_Bytes() {
    final byte[] initial = {
      (byte) 0x00, (byte) 0xff, (byte) 0xde, (byte) 0xad,
      (byte) 0xbe, (byte) 0xef, (byte) 0xc0, (byte) 0xff,
      (byte) 0xee
    };

    final String str = getImpl().fromBytes(initial);
    final byte[] array = getImpl().fromString(str);

    assertArrayEquals(initial, array);
  }
}
