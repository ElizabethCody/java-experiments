package sh.cody.string.hex.benchmark;

import sh.cody.string.hex.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class HexStringConverterBenchmark {
  private static final char[] HEXADECIMAL_LOWERCASE = {
    '0', '1', '2', '3', '4', '5', '6', '7',
    '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
  };

  private static final Random RANDOM = new SecureRandom();

  public static void main(final String... args) {
    final Logger logger = Logger.getLogger(HexStringConverter.class.getName());

    final HexStringConverter[] impls = {
      //new NaiveHexStringConverter(),
      new FastHexStringConverter(),
      new FasterHexStringConverter(),
      new FastererHexStringConverter(),
    };

    runChallenge(logger, impls, 10_000, 16);
    runChallenge(logger, impls, 10_000, 32);
    runChallenge(logger, impls, 10_000, 64);
    runChallenge(logger, impls, 100_000, 16);
    runChallenge(logger, impls, 100_000, 32);
    runChallenge(logger, impls, 100_000, 64);
    runChallenge(logger, impls, 1_000_000, 16);
    runChallenge(logger, impls, 1_000_000, 32);
    runChallenge(logger, impls, 1_000_000, 64);
  }

  private static void runChallenge(final Logger logger, final HexStringConverter[] impls, final int count, final int octets) {
    final List<String> challenge = buildChallenge(count, octets);

    for (final HexStringConverter impl : impls) {
      challenge(logger, impl, challenge);
    }
  }

  private static List<String> buildChallenge(final int count, final int octets) {
    return IntStream.range(0, count)
      .mapToObj(i -> randomHexString(octets))
      .collect(Collectors.toUnmodifiableList());
  }

  private static void challenge(final Logger logger, final HexStringConverter implementation, final List<String> challenge) {
    final int strings = challenge.size();
    final int octets = challenge.get(0).length() / 2; // assume strings are uniform

    logger.info(
      "Performing challenge on " + implementation.getClass().getSimpleName() +
      ", string count: " + strings + ", octets per string: " + octets
    );

    final long start = System.currentTimeMillis();
    
    final long rttFails = challenge.stream()
      .map(s -> new RoundTripTest(s, implementation.fromBytes(implementation.fromString(s))))
      .filter(Predicate.not(RoundTripTest::passed))
      .count();

    final long stop = System.currentTimeMillis();
    
    final double durSecs = (stop - start) / 1000.0;
    
    if (rttFails > 0) {
      logger.warning(
        String.format(
          "Test of %s (%d, %d) encountered in %d round-trip test FAILURES in" +
          " %.2f seconds.", implementation.getClass().getSimpleName(), strings,
          octets, rttFails, durSecs
        )
      );
    } else {
      logger.info(
        String.format(
          "Test of %s (%d, %d) passed in %.2f seconds.",
          implementation.getClass().getSimpleName(), strings, octets, durSecs
        )
      );
    }
  }
  
  private static final class RoundTripTest {
    private final String expected;
    private final String actual;
    
    RoundTripTest(final String expected, final String actual) {
      this.expected = expected;
      this.actual = actual;
    }
    
    public boolean passed() {
      return Objects.equals(this.expected, this.actual);
    }
    
    @SuppressWarnings("StringEquality")
    public boolean passedIgnoreCase() {
      return this.expected == this.actual ||
        this.expected != null && this.expected.equalsIgnoreCase(this.actual);
    }
  }

  private static String randomHexString(final int octets) {
    final StringBuilder builder = new StringBuilder(octets << 1);
    for (int i = 0; i < octets; ++i) {
      builder.append(HEXADECIMAL_LOWERCASE[RANDOM.nextInt(16)]);
      builder.append(HEXADECIMAL_LOWERCASE[RANDOM.nextInt(16)]);
    }
    return builder.toString();
  }
}
