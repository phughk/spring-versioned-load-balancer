package com.kaznowski.versionedloadbalancer;

import org.apache.commons.lang.NullArgumentException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class MajorMinorTest {
  @Test
  void create_with_total_version() {
    MajorMinor majorMinor = new MajorMinor(2, 3);

    assertEquals(2, majorMinor.getMajor());
    assertEquals(3, majorMinor.getMinor().get());
  }

  @Test
  void create_without_minor() {
    MajorMinor majorMinor = new MajorMinor(3, null);

    assertEquals(3, majorMinor.getMajor());
    assertFalse(majorMinor.getMinor().isPresent());
  }

  @Test
  void not_create_without_major() {
    assertThrows(NullArgumentException.class, () -> new MajorMinor(null, 2));
  }

  @Test
  void sort_versions() {
    List<MajorMinor> expectedVersions = Arrays.asList(
            new MajorMinor(1,0),
            new MajorMinor(1, 2),
            new MajorMinor(1, 3),
            new MajorMinor(1, null),
            new MajorMinor(2, 2),
            new MajorMinor(3, 1)
    );

    List<MajorMinor> shuffled = new ArrayList<>(expectedVersions);
    Collections.shuffle(shuffled);

    Collections.sort(shuffled);
    assertThat(shuffled.toString(), shuffled, IsIterableContainingInOrder.contains(expectedVersions));
  }
}
