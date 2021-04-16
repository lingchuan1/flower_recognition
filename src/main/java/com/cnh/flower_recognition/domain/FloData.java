package com.cnh.flower_recognition.domain;

import lombok.Data;

@Data
public class FloData {
    private Integer id;
    private String floImg;
    private String floName;
    private String floEnName;
    private String floDetails;
    private Double floConfidence;
    private String floLink;
}
