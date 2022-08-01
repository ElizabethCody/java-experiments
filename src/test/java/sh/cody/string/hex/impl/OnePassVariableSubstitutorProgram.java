package sh.cody.string.hex.impl;

import sh.cody.string.interpolate.ShellStyleStringInterpolator;
import sh.cody.string.interpolate.Context;
import sh.cody.string.interpolate.StringInterpolator;

public class OnePassVariableSubstitutorProgram {
  public static void main(final String... args) {
    StringInterpolator varSub = new ShellStyleStringInterpolator();
    System.out.println(varSub.interpolate("Hi, %USER% ${NXUSER:asdsadasd}!", Context.systemEnvironment()));
  }
}
