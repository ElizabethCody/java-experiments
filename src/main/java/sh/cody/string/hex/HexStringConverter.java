package sh.cody.string.hex;

public interface HexStringConverter {
  String fromBytes(byte[] bytes, boolean uppercase);

  default String fromBytes(final byte[] bytes) {
    return fromBytes(bytes, false);
  }

  byte[] fromString(String str);
}
