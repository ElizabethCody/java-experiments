package sh.cody.string.interpolate;

/**
 * Command shell-style implementation of the {@link StringInterpolator}
 * interface. This implementation interpolates values into strings in a similar
 * manner to variable substitution in shells on *nix and DOS.
 * <p>
 * Both DOS-style expressions, e.g. {@code %APPDATA%}, and sh-style expressions,
 * e.g. {@code ${PATH}}, can be interpolated with this object. Default values
 * for unmatched expressions can be specified in sh-style expressions, e.g.
 * {@code ${JAVA_HOME:/opt/java}}. When an expression cannot be matched and no
 * default value is specified, the expression will not be interpolated.
 * <p>
 * The semantics of this object are configurable and may be adjusted using the
 * flags specified at construction. Support for DOS-style expressions and
 * sh-style expressions may be enabled or disabled, respectively. Furthermore,
 * support for default values within sh-style expressions may be enabled or
 * disabled.
 *
 * @implNote This implementation is designed to perform a majority of its
 * parsing in a single pass. This results in significantly improved throughput
 * when compared to an implementation backed by regular expressions.
 *
 * @author Liz Cody <a href="mailto:liz@cody.sh">&lt;liz@cody.sh&gt;</a>
 */
public class ShellStyleStringInterpolator implements StringInterpolator {
  /**
   * The character used to indicate the beginning and end of a DOS-style
   * variable expressions.
   */
  private static final char DOS_EXPRESSION_BORDER = '%';

  /**
   * The character used to indicate the beginning of an sh-style variable.
   */
  private static final char SH_SENTINEL = '$';

  /**
   * The character used to indicate that the next sh-style sentinel is to be
   * parsed as the raw character without variable expression substitution.
   */
  private static final char SH_ESCAPE = '\\';

  /**
   * The character used to indicate the beginning of an sh-style variable
   * expression.
   */
  private static final char SH_EXPRESSION_OPENER = '{';

  /**
   * The character used to indicate the end of an sh-style variable expression.
   */
  private static final char SH_EXPRESSION_CLOSER = '}';

  /**
   * The character used to delimit the variable name and default value within
   * an sh-style variable expression.
   */
  private static final char SH_EXPRESSION_DEFAULT_VALUE_SEPARATOR = ':';

  /**
   * Whether DOS-style variables are supported.
   */
  private final boolean supportDos;

  /**
   * Whether default values within sh-style variables are supported.
   */
  private final boolean supportShDefaults;

  /**
   * Whether sh-style variables are supported.
   */
  private final boolean supportSh;

  /**
   * Constructs a new {@link ShellStyleStringInterpolator}.
   * <p>
   * The constructed {@link StringInterpolator} supports interpolating sh-style
   * variables with or without default values and supports interpolating
   * DOS-style variables.
   */
  public ShellStyleStringInterpolator() {
    this(true, true, true);
  }

  /**
   * Constructs a new {@link ShellStyleStringInterpolator}.
   * <p>
   * The constructed {@link StringInterpolator} supports interpolating sh-style
   * and DOS-style variables. Support for default values within sh-style
   * variables is provided conditionally based on the {@code shAllowDefaults}
   * parameter.
   *
   * @param shAllowDefaults whether default values should be interpreted in
   *                        sh-style variables.
   */
  public ShellStyleStringInterpolator(final boolean shAllowDefaults) {
    this(true, shAllowDefaults, true);
  }

  /**
   * Constructs a new {@link ShellStyleStringInterpolator}.
   * <p>
   * The constructed {@link StringInterpolator} supports interpolating sh-style
   * variables. Support for default values within sh-style variables and support
   * for substituting DOS-style variables is provided conditionally based on the
   * {@code shAllowDefaults} and {@code dosEnable} parameters, respectively.
   *
   * @param shAllowDefaults whether default values should be interpreted in
   *                        sh-style variables.
   * @param dosEnable       whether DOS-style variables should be substituted
   */
  public ShellStyleStringInterpolator(final boolean shAllowDefaults,
                                      final boolean dosEnable) {
    this(true, shAllowDefaults, dosEnable);
  }

  /**
   * Constructs a new {@link ShellStyleStringInterpolator}.
   * <p>
   * The constructed {@link StringInterpolator} conditionally supports
   * interpolating DOS-style variables and sh-style variables. Support for
   * interpolating sh-style variables, support for default values within
   * sh-style variables, and support for interpolating DOS-style variables is
   * provided conditionally based on the {@code shEnable}, {@code
   * shAllowDefaults}, and {@code dosEnable} parameters, respectively.
   *
   * @param shEnable        whether sh-style variables should be interpolated
   * @param shAllowDefaults whether default values should be interpreted in
   *                        sh-style variables.
   * @param dosEnable       whether DOS-style variables should be interpolated
   */
  public ShellStyleStringInterpolator(final boolean shEnable,
                                      final boolean shAllowDefaults,
                                      final boolean dosEnable) {
    this.supportDos = dosEnable;
    this.supportShDefaults = shAllowDefaults;
    this.supportSh = shEnable;
  }

  /**
   *  Interpolates values from the specified context into the specified string
   *  using shell-style variable substitution semantics.
   *
   * @param string  the string to be interpolated
   * @param context the context from which interpolated values will be retrieved
   *
   * @return the string interpolated using shell-style variable substitution
   * semantics
   */
  @Override
  public String interpolate(final String string,
                            final Context context) {
    final StringBuilder substituted = new StringBuilder(string.length() * 2);
    final StringBuilder escapeBuffer = new StringBuilder(1);
    final int length = string.length();
    int parserIndex = 0;

    while (parserIndex < length) {
      final char ch = string.charAt(parserIndex);

      if (escapeBuffer.length() > 0) {
        if (ch != SH_SENTINEL) {
          substituted.append(escapeBuffer);
        }

        escapeBuffer.setLength(0);
        substituted.append(ch);
      } else if (ch == DOS_EXPRESSION_BORDER && this.supportDos) {
        parserIndex = parseDosStyle(
          substituted, string, parserIndex, context
        );

        continue;
      } else if (ch == SH_SENTINEL && this.supportSh) {
        parserIndex = parseShStyle(
          substituted, string, parserIndex, context
        );

        continue;
      } else if (ch == SH_ESCAPE && this.supportSh) {
        escapeBuffer.append(ch);
      } else {
        substituted.append(ch);
      }

      ++parserIndex;
    }

    return substituted.append(escapeBuffer).toString();
  }

  /**
   * Parses a DOS-style variable expression.
   *
   * @param destination the destination buffer for the parsed expression
   * @param string      the source buffer
   * @param startIndex  the starting index of the DOS-style expression, the
   *                    index of the initial {@code %} character
   * @param context     the context used to look up the value to interpolate
   *
   * @throws IndexOutOfBoundsException when startIndex is out of the source
   * buffer's bounds
   *
   * @throws IllegalArgumentException when the character at the startIndex in
   * the source buffer is not a DOS-style expression border character ({@code
   * %})
   *
   * @return the index of the position in the source buffer immediately after
   * the parsed expression
   */
  private int parseDosStyle(final StringBuilder destination,
                            final String string,
                            final int startIndex,
                            final Context context) {
    final int length = string.length();

    if (startIndex >= length) {
      throw new IndexOutOfBoundsException("startIndex");
    } else if (string.charAt(startIndex) != DOS_EXPRESSION_BORDER) {
      throw new IllegalArgumentException(
        "startIndex must be positioned on a DOS-style variable expression" +
        "border character. ('%')"
      );
    }

    final StringBuilder variableNameBuffer = new StringBuilder(length / 2);
    int parserIndex = startIndex + 1;

    while (parserIndex < length) {
      final char ch = string.charAt(parserIndex);

      if (ch == DOS_EXPRESSION_BORDER) {
        if (variableNameBuffer.length() > 0) {
          final String value = context.get(variableNameBuffer.toString());

          if (value == null) {
            destination
              .append(DOS_EXPRESSION_BORDER)
              .append(variableNameBuffer)
              .append(DOS_EXPRESSION_BORDER);
          } else {
            destination.append(value);
          }
        } else {
          destination.append(DOS_EXPRESSION_BORDER);
        }

        return parserIndex + 1;
      } else if (!Character.isLetterOrDigit(ch)) {
        destination
          .append(DOS_EXPRESSION_BORDER)
          .append(variableNameBuffer);

        return parserIndex;
      } else {
        variableNameBuffer.append(ch);
      }

      ++parserIndex;
    }

    destination
      .append(DOS_EXPRESSION_BORDER)
      .append(variableNameBuffer);

    return parserIndex;
  }

  /**
   * Parses a sh-style variable expression.
   *
   * @param destination the destination buffer for the parsed expression
   * @param string      the source buffer
   * @param startIndex  the starting index of the sh-style expression, the
   *                    index of the initial {@code $} character
   * @param context     the context used to look up the value to interpolate
   *
   * @throws IndexOutOfBoundsException when startIndex is out of the source
   * buffer's bounds
   *
   * @throws IllegalArgumentException when the character at the startIndex in
   * the source buffer is not a sh-style expression sentinel character ({@code
   * $})
   *
   * @return the index of the position in the source buffer immediately after
   * the parsed expression
   */
  private int parseShStyle(final StringBuilder destination,
                           final String string,
                           final int startIndex,
                           final Context context) {
    final int length = string.length();

    if (startIndex >= length) {
      throw new IndexOutOfBoundsException("startIndex");
    } else if (string.charAt(startIndex) != SH_SENTINEL) {
      throw new IllegalArgumentException(
        "startIndex must be positioned on an sh-style variable expression " +
        "sentinel character. ('$')"
      );
    } else if (startIndex + 1 >= length) {
      destination.append(SH_SENTINEL);
      return startIndex + 1;
    } else {
      final char ch = string.charAt(startIndex + 1);

      if (ch != SH_EXPRESSION_OPENER) {
        destination.append(SH_SENTINEL).append(ch);
        return startIndex + 2;
      }
    }

    final int halfLength = length / 2;
    final StringBuilder variableNameBuffer = new StringBuilder(halfLength);
    final StringBuilder escapeBuffer = new StringBuilder(1);
    StringBuilder defaultValueBuffer = null;
    int parserIndex = startIndex + 2;

    while (parserIndex < length) {
      final char ch = string.charAt(parserIndex);

      if (escapeBuffer.length() > 0) {
        if (ch != SH_EXPRESSION_DEFAULT_VALUE_SEPARATOR) {
          variableNameBuffer.append(escapeBuffer);
        }

        escapeBuffer.setLength(0);
        variableNameBuffer.append(ch);
      } else if (ch == SH_EXPRESSION_CLOSER) {
        final String name = variableNameBuffer.toString().trim();
        variableNameBuffer.setLength(0);
        variableNameBuffer.trimToSize();
        final String value = context.get(name);

        if (value == null) {
          if (defaultValueBuffer == null) {
            destination
              .append(SH_SENTINEL)
              .append(SH_EXPRESSION_OPENER)
              .append(name)
              .append(SH_EXPRESSION_CLOSER);
          } else {
            destination.append(defaultValueBuffer);
          }
        } else {
          destination.append(value);
        }

        if (defaultValueBuffer != null) {
          defaultValueBuffer.setLength(0);
          defaultValueBuffer.trimToSize();
        }

        return parserIndex + 1;
      } else if (ch == SH_EXPRESSION_DEFAULT_VALUE_SEPARATOR && supportShDefaults) {
        defaultValueBuffer = new StringBuilder(halfLength);
        parserIndex = parseShStyleDefaultValue(
          defaultValueBuffer, string, parserIndex
        );
        continue;
      } else if (ch == SH_ESCAPE) {
        escapeBuffer.append(ch);
      } else {
        variableNameBuffer.append(ch);
      }

      ++parserIndex;
    }

    destination
      .append(SH_SENTINEL)
      .append(SH_EXPRESSION_OPENER)
      .append(variableNameBuffer)
      .append(escapeBuffer);

    if (defaultValueBuffer != null) {
      destination.append(defaultValueBuffer);
    }

    return parserIndex;
  }

  /**
   * Parses a sh-style variable expression's default value.
   *
   * @param destination the destination buffer for the parsed value
   * @param string      the source buffer
   * @param startIndex  the starting index of the default value expression, the
   *                    index of the separating {@code :} character
   *
   * @throws IndexOutOfBoundsException when startIndex is out of the source
   * buffer's bounds
   *
   * @throws IllegalArgumentException when the character at the startIndex in
   * the source buffer is not a sh-style expression separating character ({@code
   * :})
   *
   * @return the index of the position in the source buffer immediately after
   * the parsed expression
   */
  private int parseShStyleDefaultValue(final StringBuilder destination,
                                       final String string,
                                       final int startIndex) {
    final int length = string.length();

    if (startIndex >= length) {
      throw new IndexOutOfBoundsException("startIndex");
    } else if (string.charAt(startIndex) != SH_EXPRESSION_DEFAULT_VALUE_SEPARATOR) {
      throw new IllegalArgumentException(
        "startIndex must be positioned on an sh-style variable expression " +
        "default value separator character. (':')"
      );
    }

    final int halfLength = length / 2;
    final StringBuilder defaultValueBuffer = new StringBuilder(halfLength);
    int parseIndex = startIndex + 1;
    boolean escaped = false;

    while (parseIndex < length) {
      final char ch = string.charAt(parseIndex);

      if (escaped) {
        if (ch != SH_EXPRESSION_OPENER && ch != SH_EXPRESSION_CLOSER) {
          defaultValueBuffer.append(SH_ESCAPE);
        }

        defaultValueBuffer.append(ch);

        escaped = false;
      } else if (ch == SH_EXPRESSION_CLOSER) {
        destination.append(defaultValueBuffer);
        return parseIndex;
      } else if (ch == SH_ESCAPE) {
        escaped = true;
      } else {
        defaultValueBuffer.append(ch);
      }

      ++parseIndex;
    }

    defaultValueBuffer.setLength(0);
    defaultValueBuffer.trimToSize();

    try {
      destination.append(string, startIndex, parseIndex);
    } catch (final IndexOutOfBoundsException ignored) {
      // nothing to append, the default value expression is empty
    }

    return parseIndex;
  }

  /**
   * Returns whether this object supports DOS-style variable expressions.
   *
   * @return whether this object supports DOS-style variable expressions
   */
  public boolean supportsDosStyle() {
    return this.supportDos;
  }

  /**
   * Returns whether this object supports sh-style variable expressions.
   *
   * @return whether this object supports sh-style variable expressions
   */
  public boolean supportsShStyle() {
    return this.supportSh;
  }

  /**
   * Returns whether this object supports default values in sh-style variable
   * expressions.
   *
   * @return whether this object supports default values in sh-style variable
   * expressions
   */
  public boolean supportsShDefaults() {
    return this.supportShDefaults;
  }
}
