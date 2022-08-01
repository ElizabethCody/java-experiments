package sh.cody.string.interpolate;

/**
 * An object that maps string keys to string values.
 *
 * @author Liz Cody <a href="mailto:liz@cody.sh">&lt;liz@cody.sh&gt;</a>
 */
public interface Context {
  /**
   * Returns the value to which the specified key is associated.
   *
   * @param key the key whose associated value is to be returned
   * @return the value to which the specified key is associated, or {@code null}
   * if this context does not contain a value associated with the key
   */
  String get(String key);

  /**
   * Constructs a {@link Context} for the system environment variables.
   *
   * @implNote this method returns {@code System::getenv}
   * @return a context for the system environment variables
   */
  static Context systemEnvironment() {
    return System::getenv;
  }

  /**
   * Constructs a {@link Context} for the system properties.
   *
   * @implNote this method returns {@code System::getProperty}
   * @return a context for the system properties
   */
  static Context systemProperties() {
    return System::getProperty;
  }

  /**
   * Constructs a {@link Context} for the system environment variables and
   * system properties.
   *
   * @implNote the returned context accesses system properties by default;
   * however, environment variables may be accessed by prefixing the key with
   * {@code env.}; likewise, system properties may be accessed explicitly by
   * prefixing the key with {@code prop.}
   *
   * @return a context for the system environment variables and system
   * properties
   */
  static Context combined() {
    return key -> {
      if (key.toLowerCase().startsWith("env.")) {
        return System.getenv(key.substring(4));
      } else if (key.toLowerCase().startsWith("prop.")) {
        return System.getProperty(key.substring(5));
      } else {
        return System.getProperty(key);
      }
    };
  }
}
