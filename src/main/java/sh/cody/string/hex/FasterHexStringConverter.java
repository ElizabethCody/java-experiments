package sh.cody.string.hex;

@SuppressWarnings("DuplicatedCode")
public class FasterHexStringConverter implements HexStringConverter {
  private static char[] HEXADECIMAL_LOWERCASE = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static char[] HEXADECIMAL_UPPERCASE = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
  };

  @Override
  public String fromBytes(final byte[] bytes, final boolean uppercase) {
    final char[] mapping = uppercase ? HEXADECIMAL_UPPERCASE :
                                       HEXADECIMAL_LOWERCASE;
    final StringBuilder builder = new StringBuilder();
    for (final byte b : bytes) {
      final int ub = b & 255;
      builder.append(mapping[ub >> 4]);
      builder.append(mapping[ub & 15]);
    }
    return builder.toString();
  }

  @Override
  public byte[] fromString(final String str) {
    final int len = str.length();

    if ((len & 1) == 1) {
      throw new NumberFormatException(
        "Input string must be composed of non-negative 2-digit zero-filled " +
        "hexadecimal octets. For input string: " + str
      );
    }

    final byte[] bytes = new byte[len >> 1];

    for (int i = 0, j = 1; j < len; i += 2, j += 2) {
      try {
        bytes[i >> 1] =
          (byte) (fromChar(str.charAt(i)) << 4 | fromChar(str.charAt(j)));
      } catch (final IllegalArgumentException ignored) {
        throw new NumberFormatException(
          "Input characters must be hexadecimal. For input string: " + str +
          ", character: " + str.charAt(i) + ", index: " + i
        );
      }
    }

    return bytes;
  }

  private static int fromChar(final char ch) {
    if (ch >= '0' && ch <= '9') {
      return (int) ch - '0';
    } else if (ch >= 'a' && ch <= 'f') {
      return (int) ch - 'a' + 10;
    } else if (ch >= 'A' && ch <= 'F') {
      return (int) ch - 'A' + 10;
    } else {
      throw new IllegalArgumentException(
        "char '" + ch + "' ordinal " + (int) ch + " is not 0-9, a-f, or A-F"
      );
    }
  }
}
