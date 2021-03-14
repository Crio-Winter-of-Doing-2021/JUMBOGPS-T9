package com.crio.jumbotail.assettracking.others;

import static org.junit.jupiter.api.Assertions.assertFalse;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled
public class BasicTests {

	@Test
	void given_timestamp_both_object_are_same() {

		long utc2_02_PM_14_mar_2021 = 1615730554;

		final Instant instant = Instant.ofEpochSecond(utc2_02_PM_14_mar_2021);
		final LocalDateTime localDateTimeFromInstant = LocalDateTime.ofInstant(instant, ZoneOffset.systemDefault());
		final LocalDateTime localDateTimeDirect = LocalDateTime.ofEpochSecond(utc2_02_PM_14_mar_2021, 0, ZoneOffset.UTC);


		assertFalse(localDateTimeDirect.isEqual(localDateTimeFromInstant));
	}

	@Test
	void some() {
		final Instant instant = Instant.now();
		final LocalDateTime localDateTimeInUtc = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);

		LocalDateTime now = LocalDateTime.now();

		System.out.println("now = " + now);
		System.out.println("localDateTimeInUtc = " + localDateTimeInUtc);

		assertFalse(now.isEqual(localDateTimeInUtc));

	}


}
