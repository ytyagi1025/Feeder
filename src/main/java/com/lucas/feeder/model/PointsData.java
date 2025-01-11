package com.lucas.feeder.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("points-data")
public class PointsData {
    @Id
    private String id;

    private String genuineCode;
    private Status status;
    private JodidaarStatus jodidaarStatus;
    private RetailerStatus retailerStatus;
}
