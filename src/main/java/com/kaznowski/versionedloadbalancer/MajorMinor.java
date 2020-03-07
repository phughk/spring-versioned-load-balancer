package com.kaznowski.versionedloadbalancer;

import org.apache.commons.lang.NullArgumentException;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

public class MajorMinor implements Comparable<MajorMinor> {

  private static final Pattern REGEX_MAJOR = Pattern.compile("v([0-9]*)");
  private static final Pattern REGEX_MINOR = Pattern.compile("v([0-9]*).([0-9]*)");

  private final Integer major;
  private final Integer minor;

  public MajorMinor(Integer major, Integer minor) {
    if (major == null) {
      throw new NullArgumentException("Major version cannot be null");
    }
    this.major = major;
    this.minor = minor;
  }

  public Optional<Integer> getMinor() {
    return Optional.ofNullable(minor);
  }

  public Integer getMajor() {
    return major;
  }

  public static MajorMinor parse(String path) {
    throw new RuntimeException("Unimplemented");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MajorMinor that = (MajorMinor) o;
    return Objects.equals(major, that.major) &&
            Objects.equals(minor, that.minor);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor);
  }

  @Override
  public int compareTo(MajorMinor o) {
    int thisVersion = major*10000 + Optional.ofNullable(minor).orElse(9000);
    int thatVersion = o.major*1000 + Optional.ofNullable(o.minor).orElse(9000);
    return thisVersion-thatVersion;
  }

  @Override
  public String toString() {
    return "MajorMinor{" +
            "major=" + major +
            ", minor=" + minor +
            '}';
  }
}
