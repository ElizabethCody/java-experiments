package sh.cody.string.hex;

@SuppressWarnings("DuplicatedCode")
public class FastHexStringConverter implements HexStringConverter {
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
    switch (ch) {
      case '0': return 0x0;
      case '1': return 0x1;
      case '2': return 0x2;
      case '3': return 0x3;
      case '4': return 0x4;
      case '5': return 0x5;
      case '6': return 0x6;
      case '7': return 0x7;
      case '8': return 0x8;
      case '9': return 0x9;
      case 'A':
      case 'a': return 0xa;
      case 'B':
      case 'b': return 0xb;
      case 'C':
      case 'c': return 0xc;
      case 'D':
      case 'd': return 0xd;
      case 'E':
      case 'e': return 0xe;
      case 'F':
      case 'f': return 0xf;
      default: throw new IllegalArgumentException(
        "char '" + ch + "' ordinal " + (int) ch + " is not 0-9, a-f, or A-F"
      );
    }
  }
}
