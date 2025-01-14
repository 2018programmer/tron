package com.dx.futures.local;

import java.util.concurrent.atomic.AtomicInteger;

public final class Constants {

    // TODO use config to decide how engines to start
    public static final int NUM_KIDS = 4;

    public static final AtomicInteger ID = new AtomicInteger();

    public static final String[] PRODUCT_IDS = { "0", "1", "2", "3", "4", "5",
	    "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17",
	    "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
	    "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
	    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50",
	    "51", "52", "53", "54", "55", "56", "57", "58", "59", "60", "61",
	    "62", "63", "64", "65", "66", "67", "68", "69", "70", "71", "72",
	    "73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83",
	    "84", "85", "86", "87", "88", "89", "90", "91", "92", "93", "94",
	    "95", "96", "97", "98", "99" };

    public static final long DELAY = 3; // how many milliseconds between
					// trading
					// sessions

    public static final long TIMEOUT = 60000; // num ms after which incomplete
					      // SOs and POs should be removed

}
