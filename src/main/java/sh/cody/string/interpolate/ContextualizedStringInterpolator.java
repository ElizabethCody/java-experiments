package sh.cody.string.interpolate;

/**
 * An object that acts as a {@link StringInterpolator} with a fixed
 * {@link Context}.
 *
 * @author Liz Cody <a href="mailto:liz@cody.sh">&lt;liz@cody.sh&gt;</a>
 */
public interface ContextualizedStringInterpolator {
  String interpolate(String string);
}
