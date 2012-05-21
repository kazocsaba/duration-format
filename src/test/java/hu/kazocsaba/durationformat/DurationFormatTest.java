package hu.kazocsaba.durationformat;

import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Kazó Csaba
 */
public class DurationFormatTest {

	@Test
	public void testLowUnit() {
		new DurationFormat().timeUnitLevel(2).lowestTimeUnit(TimeUnit.MILLISECONDS).format(1462912);
	}

	@Test
	public void testExamples() {
		assertEquals("13 s 499 μs", new DurationFormat().timeUnitLevel(3).format(TimeUnit.SECONDS.toNanos(13) + TimeUnit.MICROSECONDS.toNanos(499)));
		assertEquals("93 m 6 s 486 ms", new DurationFormat().timeUnitLevel(3).format(TimeUnit.MINUTES.toNanos(93) + TimeUnit.SECONDS.toNanos(6) + TimeUnit.MILLISECONDS.toNanos(486)));
		
		DurationFormat durationFormat;
		durationFormat=new DurationFormat()
				.setDropInnerZeroes(false)
				.timeUnit(TimeUnit.MILLISECONDS)
				.timeUnitLevel(3);
		
		assertEquals("360 ns", durationFormat.format(360)); assertFalse(durationFormat.isDisplayedAsZero(360, TimeUnit.NANOSECONDS));
		assertEquals("1500 ms 0 μs 0 ns", durationFormat.format(1500000000)); assertFalse(durationFormat.isDisplayedAsZero(1500000000, TimeUnit.NANOSECONDS));
		assertEquals("60 ms 1 μs 3 ns", durationFormat.format(60001003)); assertFalse(durationFormat.isDisplayedAsZero(60001003, TimeUnit.NANOSECONDS));
		
		durationFormat=new DurationFormat()
				.timeUnit(TimeUnit.SECONDS);
		
		assertEquals("0 s", durationFormat.format(360)); assertTrue(durationFormat.isDisplayedAsZero(360, TimeUnit.NANOSECONDS));
		assertEquals("158 s", durationFormat.format(158334286578L)); assertFalse(durationFormat.isDisplayedAsZero(158334286578L, TimeUnit.NANOSECONDS));
		
		durationFormat=new DurationFormat()
				.lowestTimeUnit(TimeUnit.MILLISECONDS);
		
		assertEquals("0 ms", durationFormat.format(360)); assertTrue(durationFormat.isDisplayedAsZero(360, TimeUnit.NANOSECONDS));
		assertEquals("1500 ms", durationFormat.format(1500401823)); assertFalse(durationFormat.isDisplayedAsZero(1500401823, TimeUnit.NANOSECONDS));
		assertEquals("3 m", durationFormat.format(158334286578L)); assertFalse(durationFormat.isDisplayedAsZero(158334286578L, TimeUnit.NANOSECONDS));
	}
}