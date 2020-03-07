package com.kaznowski.versionedloadbalancer;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.NullArgumentException;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Log4j2
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
    if (o.major > this.major) {
      return -1;
    }
    if (o.major < this.major) {
      return 1;
    }
    if (o.minor==null) {
      if (this.minor==null) {
        return 0;
      }
      return -1;
    }
    if (this.minor==null) {
      return 1;
    }
    return this.minor.compareTo(o.minor);
  }

  @Override
  public String toString() {
    return "MajorMinor{" +
        "major=" + major +
        ", minor=" + minor +
        '}';
  }
}
