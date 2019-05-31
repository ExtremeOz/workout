package com.a_track_it.fitdata.common.model;

public interface Processor {
    boolean processPoint(long timestamp, float value1, float value2, float value3);
}
