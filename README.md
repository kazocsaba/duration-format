Duration format
===============

This library provides the class `DurationFormat` that can be used
to create string representations of time durations. The primary
feature of the library is that the time unit is selected
automatically based on the input. The following example outputs
are created using the same configuration:

    13 s 499 μs
    93 m 6 s 486 ms

Using
-----

The library resides in the central Maven repository with group ID `hu.kazocsaba`
and artifact ID `duration-format`. If you use a project management system which
can fetch dependencies from there, you can just add the library as a dependency. E.g.
in Maven:

    <dependency>
        <groupId>hu.kazocsaba</groupId>
        <artifactId>duration-format</artifactId>
        <version>a.b.c</version>
    </dependency>

Example
-------

Quick and dirty benchmarking:

    long startTime = System.nanoTime();
    // run something
    long endTime = System.nanoTime();
    System.out.println("Finished in " + new DurationFormat().format(end - start));

Customizing the behaviour:

    DurationFormat durationFormat = new DurationFormat()
        .setDropInnerZeroes(false)
        .timeUnit(TimeUnit.MILLISECONDS)        // never display a time unit higher than milliseconds
        .timeUnitLevel(3);                      // allow milliseconds, microseconds, and nanoseconds
    
    durationFormat.format(360);                 // -> "360 ns"
    durationFormat.format(150000000);           // -> "1500 ms 0 μs 0 ns"
    durationFormat.format(60001003);            // -> "60 ms 1 μs 3 ns"
    
    durationFormat = new DurationFormat()
        .timeUnit(TimeUnit.SECONDS);            // always show the duration in seconds
                                                // .timeUnitLevel(1) is the default
    
    durationFormat.format(360);                 // -> "0 s"
    durationFormat.format(158334286578L);       // -> "158 s"
    
    durationFormat = new DurationFormat()       // choose the time unit automatically
        .lowestTimeUnit(TimeUnit.MILLISECONDS); // but we're not interested in anything below milliseconds
    
    durationFormat.format(360);                 // -> "0 ms"
    durationFormat.format(1500401823);          // -> "1500 ms"
    durationFormat.format(158334286578L);       // -> "3 m"
