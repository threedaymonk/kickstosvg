package uk.sanshinkai.kickstosvg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import uk.sanshinkai.kickstosvg.FuriganaStringParser;

class FuriganaStringParserTest {
  @Test
  public void testEmptyString() {
    var parsed = new FuriganaStringParser().parse("");

    assertTrue(parsed.isEmpty());
  }

  @Test
  public void testSimpleString() {
    var parsed = new FuriganaStringParser().parse("ABC");

    assertEquals(1, parsed.size());

    assertEquals("ABC", parsed.get(0).surface());
    assertNull(parsed.get(0).reading());
  }

  @Test
  public void testStringWithReading() {
    var parsed = new FuriganaStringParser().parse("{ABC}{DEF}");

    assertEquals(1, parsed.size());

    assertEquals("ABC", parsed.get(0).surface());
    assertEquals("DEF", parsed.get(0).reading());
  }

  @Test
  public void testComplexString() {
    var parsed = new FuriganaStringParser().parse("AA{BB}{CC}DD");

    assertEquals(3, parsed.size());

    assertEquals("AA", parsed.get(0).surface());
    assertNull(parsed.get(0).reading());

    assertEquals("BB", parsed.get(1).surface());
    assertEquals("CC", parsed.get(1).reading());

    assertEquals("DD", parsed.get(2).surface());
    assertNull(parsed.get(2).reading());
  }
}
