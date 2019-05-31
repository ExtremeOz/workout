package com.a_track_it.fitdata.common.model;

public class SystemOutProcessor implements Processor {
    public SystemOutProcessor(){}

    @Override
    public boolean processPoint(long timestamp, float value1, float value2, float value3) {
        System.out.format("%d, %f, %f, %f\n", timestamp, value1, value2, value3);
        return false;
    }
}
