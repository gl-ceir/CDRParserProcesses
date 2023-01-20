package com.glocks.parser;

import javax.sound.sampled.Port;

public class CDRColumn {

    String columName;
    String graceType;
    String postGraceType;

    public CDRColumn(String columnName, String graceType, String postGraceType) {
        this.columName = columnName;
        this.graceType = graceType;
        this.postGraceType = postGraceType;
    }

}

