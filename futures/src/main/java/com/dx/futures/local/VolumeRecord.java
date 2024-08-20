package com.dx.futures.local;

import java.util.Date;
import java.util.List;

public  class VolumeRecord {
	public static final VolumeRecord EMPTY = new VolumeRecord(null, 0, 0,
		null, 0);
	public String productId;
	public int numberOfSales;
	public double turnover;
	public Date timestamp;
	public int count;

	public VolumeRecord(String productId, int numberOfSales,
		double turnover, Date timestamp, int count) {
	    this.productId = productId;
	    this.numberOfSales = numberOfSales;
	    this.turnover = turnover;
	    this.timestamp = timestamp;
	    this.count = count;
	}

	public static VolumeRecord add(VolumeRecord a, VolumeRecord b) {
	    return new VolumeRecord(b.productId, a.numberOfSales
		    + b.numberOfSales, a.turnover + b.turnover, null, a.count
		    + b.count);
	}

	public static VolumeRecord aggregate(List<VolumeRecord> vrs) {
	    VolumeRecord vr = EMPTY;
	    vr = vrs.stream().reduce(vr, VolumeRecord::add);
	    return vr;

	}
    }