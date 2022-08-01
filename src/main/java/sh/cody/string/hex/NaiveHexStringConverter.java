package sh.cody.string.hex;

import java.util.Formatter;
import java.util.Locale;

@SuppressWarnings("DuplicatedCode")
public class NaiveHexStringConverter implements HexStringConverter {
  @Override
  public String fromBytes(final byte[] bytes, final boolean uppercase) {
    final String format = uppercase ? "%02X" : "%02x";
    final StringBuilder builder = new StringBuilder();
    try (final Formatter formatter = new Formatter(builder, Locale.ROOT)) {
      for (byte b : bytes) {
        formatter.format(format, b);
      }
    }
    return builder.toString();
  }

  @Override
  public byte[] fromString(final String str) {
    if (str.length() % 2 != 0 && !str.contains("-")) {
      throw new NumberFormatException(
        "Input string must be composed of non-negative 2-digit zero-filled " +
          "hexadecimal octets. For input string: " + str
      );
    }

    final byte[] bytes = new byte[str.length() / 2];

    for (int i = 0, j = 2; j <= str.length(); i += 2, j+= 2) {
      final String octet = str.substring(i, j);
      try {
        bytes[i / 2] = (byte) Integer.parseInt(octet, 16);
      } catch (final NumberFormatException exception) {
        final NumberFormatException detailed = new NumberFormatException(
          "Input string octets must be hexadecimal. For octet: " + octet + " " +
            "For input string: " + str
        );
        throw (NumberFormatException) detailed.initCause(exception);
      }
    }

    return bytes;
  }
}
