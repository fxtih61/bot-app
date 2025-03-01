package com.openjfx;

/**
 * Utility class for retrieving system information. Provides methods to get the Java and JavaFX
 * version.
 *
 * @author mian
 */
public class SystemInfo {

  /**
   * Returns the version of Java currently running.
   *
   * @return the Java version as a String
   * @author mian
   */
  public static String javaVersion() {
    return System.getProperty("java.version");
  }

  /**
   * Returns the version of JavaFX currently running.
   *
   * @return the JavaFX version as a String
   * @author mian
   */
  public static String javafxVersion() {
    return System.getProperty("javafx.version");
  }

}