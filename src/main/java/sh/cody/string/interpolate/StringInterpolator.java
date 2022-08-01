package sh.cody.string.interpolate;

/**
 * An object that interpolates values from a given {@link Context} into a
 * {@link String}.
 *
 * @author Liz Cody <a href="mailto:liz@cody.sh">&lt;liz@cody.sh&gt;</a>
 */
public interface StringInterpolator {
  /**
   * Interpolates the specified string with values from the specified context.
   *
   * @param string  the string to be interpolated
   * @param context the context from which interpolated values will be retrieved
   *
   * @return the interpolated string
   */
  String interpolate(String string, Context context);

  /**
   * Constructs a {@link ContextualizedStringInterpolator} from this {@link
   * StringInterpolator} and the specified {@link Context}.
   *
   * @param context the context from which interpolated values may be retrieved
   *
   * @return a contextualized string interpolator for the specified context
   * backed by this string interpolator
   */
  default ContextualizedStringInterpolator forContext(final Context context) {
    return string -> this.interpolate(string, context);
  }
}
