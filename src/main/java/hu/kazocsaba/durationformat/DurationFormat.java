package hu.kazocsaba.durationformat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * {@code DurationFormat} instances provide formatting for time durations. Some example output:
 * <pre>
 * 13 s 499 μs
 * 93 m 6 s 486 ms</pre>
 * The output consists of fields which correspond to the time units defined in {@link TimeUnit}. The input and the
 * configuration of the {@code DurationFormat} determines which of the fields are present.
 * <p>
 * The first field which appears is the primary time unit of the duration. Its value can be set with the
 * {@link #timeUnit(TimeUnit)} function. If its value is {@code null}, then the primary time unit is selected
 * automatically for each input.
 * The last, lowest time unit is determined by two factors. It can be specified explicitly using
 * {@link #lowestTimeUnit(TimeUnit)}, and it is limited relative to the primary time unit with
 * {@link #timeUnitLevel(int)}, where the level defines the maximum number of consecutive time units allowed.
 * <p>
 * Display of zero-valued fields is controlled through two flags. Zeroes appearing before the first non-zero field
 * are removed if {@link #setDropLeadingZeroes(boolean) dropLeadingZeroes} is set, unless all fields would be zero, in
 * which case the primary time unit is printed. After the first non-zero field, the behaviour is specified by the
 * {@link #setDropInnerZeroes(boolean) dropInnerZeroes} flag.
 * <p>
 * By default, only the primary time unit is displayed, and zeroes are dropped.
 * 
 * @author Kazó Csaba
 */
public class DurationFormat {
	private TimeUnit mandatoryUnit=null;
	private TimeUnit lowestUnit=null;
	/** The number of time unit levels to display. */
	private int levels=1;
	private boolean dropLeadingZeroes=true;
	private boolean dropInnerZeroes=true;

	/**
	 * Sets the dropInnerZeroes flag. If this flag is {@code true} (default), then all zero fields after the first non-zero one
	 * are removed from the output.
	 * @param dropInnerZeroes the new value of the flag
	 * @return this object
	 */
	public DurationFormat setDropInnerZeroes(boolean dropInnerZeroes) {
		this.dropInnerZeroes = dropInnerZeroes;
		return this;
	}
	/**
	 * Sets the dropLeadingZeroes flag. If this flag is {@code true} (default), then all zero fields before the first non-zero one
	 * are removed from the output, unless there is only one field and it is zero.
	 * @param dropLeadingZeroes the new value of the flag
	 * @return this object
	 */
	public DurationFormat setDropLeadingZeroes(boolean dropLeadingZeroes) {
		this.dropLeadingZeroes = dropLeadingZeroes;
		return this;
	}
	/**
	 * Sets the primary time unit to display. If it is set to {@code null} (default), then a time unit is automatically selected for each
	 * input.
	 * @param unit the primary time unit, or null for automatic selection
	 * @return this object
	 */
	public DurationFormat timeUnit(TimeUnit unit) {
		mandatoryUnit=unit;
		return this;
	}
	
	/**
	 * Sets the lowest time unit to display.
	 * @param unit the lowest allowed time unit; if it is {@code null} (default), then only the {@code timeUnitLevel} property
	 * restricts the displayed fields
	 * @return this object
	 */
	public DurationFormat lowestTimeUnit(TimeUnit unit) {
		lowestUnit=unit;
		return this;
	}

	/**
	 * Returns the primary time unit to display, or null if the time unit is selected automatically (default).
	 * @return the configured primary time unit
	 */
	public TimeUnit getTimeUnit() {
		return mandatoryUnit;
	}

	/**
	 * Sets the number of time unit levels to display. E.g. if the primary time unit is set to
	 * seconds and the level is 2, then 2300 milliseconds is displayed as "2 s 300 ms";
	 * if level is 1, then it is displayed as "2 s".
	 * <p>
	 * The default value is 1.
	 * @param level the maximum number of time units
	 * @return this object
	 * @throws IllegalArgumentException if {@code level<=0}
	 */
	public DurationFormat timeUnitLevel(int level) {
		if (level<=0) throw new IllegalArgumentException();
		levels=level;
		return this;
	}

	private static final List<TimeUnit> UNITS=Arrays.asList(
			TimeUnit.NANOSECONDS,
			TimeUnit.MICROSECONDS,
			TimeUnit.MILLISECONDS,
			TimeUnit.SECONDS,
			TimeUnit.MINUTES,
			TimeUnit.HOURS,
			TimeUnit.DAYS);
	private static final long[] NANO_COUNTS={
		1L,
		1000L,
		1000L*1000,
		1000L*1000*1000,
		1000L*1000*1000*60,
		1000L*1000*1000*60*60,
		1000L*1000*1000*60*60*24
	};
	private static final int[] CONV_NUM={
		1000,
		1000,
		1000,
		60,
		60,
		24
	};
	private static final String[] UNIT_STRING={
		"ns",
		"μs",
		"ms",
		"s",
		"m",
		"h",
		"d"
	};
	/**
	 * Returns {@code true} if the specified duration would produce only zero-valued fields.
	 * @param time the duration
	 * @param unit the time unit of the duration
	 * @return {@code true} if only zero fields would be present in the formatted duration
	 */
	public boolean isDisplayedAsZero(long time, TimeUnit unit) {
		return isDisplayedAsZero(unit.toNanos(time));
	}
	private boolean isDisplayedAsZero(long timeNano) {
		int mainIndex;
		if (mandatoryUnit==null) {
			if (timeNano<NANO_COUNTS[1]*10)
				mainIndex=0;
			else if (timeNano<NANO_COUNTS[2]*10)
				mainIndex=1;
			else if (timeNano<NANO_COUNTS[3]*10)
				mainIndex=2;
			else if (timeNano<NANO_COUNTS[3]*100) // seconds up to 100 s
				mainIndex=3;
			else if (timeNano<NANO_COUNTS[4]*100) // minutes up to 100 m
				mainIndex=4;
			else if (timeNano<NANO_COUNTS[5]*100) // hours up to 100 h
				mainIndex=5;
			else
				mainIndex=6;
		} else {
			mainIndex=UNITS.indexOf(mandatoryUnit);
		}
		int lastIndex=Math.max(0, mainIndex-levels+1);
		if (lowestUnit!=null) {
			int specifiedLastIndex=UNITS.indexOf(lowestUnit);
			if (mandatoryUnit==null && mainIndex<specifiedLastIndex) {
				// the automatic main unit is lower than the specified lowest; override it
				lastIndex=specifiedLastIndex;
				mainIndex=specifiedLastIndex;
			} else {
				lastIndex=Math.max(lastIndex, Math.min(specifiedLastIndex, mainIndex));
			}
		}
		long timeInLastUnit=timeNano/NANO_COUNTS[lastIndex];
		if (lastIndex>0) {
			// round up if necessary
			if (timeNano-timeInLastUnit*NANO_COUNTS[lastIndex]>=NANO_COUNTS[lastIndex]/2)
				timeInLastUnit++;
		}
		return timeInLastUnit==0;
	}
	/**
	 * Formats a duration.
	 * @param time the duration
	 * @param unit the time unit of the duration
	 * @return the formatted string
	 */
	public String format(long time, TimeUnit unit) {
		return format(unit.toNanos(time));
	}
	/**
	 * Formats a duration.
	 * @param timeNano the duration in nanoseconds
	 * @return the formatted string
	 */
	public String format(long timeNano) {
		// mainIndex will be the index of the highest time unit we use, lastIndex will specify the lowest
		
		int mainIndex;
		if (mandatoryUnit==null) {
			if (timeNano<NANO_COUNTS[1]*10)
				mainIndex=0;
			else if (timeNano<NANO_COUNTS[2]*10)
				mainIndex=1;
			else if (timeNano<NANO_COUNTS[3]*10)
				mainIndex=2;
			else if (timeNano<NANO_COUNTS[3]*100) // seconds up to 100 s
				mainIndex=3;
			else if (timeNano<NANO_COUNTS[4]*100) // minutes up to 100 m
				mainIndex=4;
			else if (timeNano<NANO_COUNTS[5]*100) // hours up to 100 h
				mainIndex=5;
			else
				mainIndex=6;
		} else {
			mainIndex=UNITS.indexOf(mandatoryUnit);
		}
		int lastIndex=Math.max(0, mainIndex-levels+1);
		if (lowestUnit!=null) {
			int specifiedLastIndex=UNITS.indexOf(lowestUnit);
			if (mandatoryUnit==null && mainIndex<specifiedLastIndex) {
				// the automatic main unit is lower than the specified lowest; override it
				lastIndex=specifiedLastIndex;
				mainIndex=specifiedLastIndex;
			} else {
				lastIndex=Math.max(lastIndex, Math.min(specifiedLastIndex, mainIndex));
			}
		}
		if (lastIndex>mainIndex) throw new AssertionError("Error in DurationFormat; time="+timeNano+", mainIndex="+mainIndex+", lastIndex="+lastIndex);
		long timeInLastUnit=timeNano/NANO_COUNTS[lastIndex];
		if (lastIndex>0) {
			// round up if necessary
			if (timeNano-timeInLastUnit*NANO_COUNTS[lastIndex]>=NANO_COUNTS[lastIndex]/2)
				timeInLastUnit++;
		}
		long[] times=new long[mainIndex-lastIndex+1];
		times[0]=timeInLastUnit;
		for (int level=lastIndex+1; level<=mainIndex; level++) {
			times[level-lastIndex]=times[level-lastIndex-1]/CONV_NUM[level-1];
			times[level-lastIndex-1]-=times[level-lastIndex]*CONV_NUM[level-1];
		}
		
		// assemble the string
		
		StringBuilder sb=new StringBuilder();
		boolean hasNonZero=false;
		for (int level=mainIndex; level>=lastIndex; level--) {
			hasNonZero=hasNonZero || (times[level-lastIndex]>0);
			if (times[level-lastIndex]>0 || (hasNonZero ? !dropInnerZeroes : (!dropLeadingZeroes || level==lastIndex))) {
				sb.append(times[level-lastIndex]);
				sb.append(' ');
				sb.append(UNIT_STRING[level]);
				sb.append(' ');
			}
		}
		// delete trailing space
		sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
}
