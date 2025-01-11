package com.lucas.feeder.dao;

import com.lucas.feeder.model.JodidaarStatus;
import com.lucas.feeder.model.PointsData;
import com.lucas.feeder.model.RetailerStatus;
import com.lucas.feeder.model.Status;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends MongoRepository<PointsData, String> {
    PointsData findFirstByStatusAndJodidaarStatus(Status status, JodidaarStatus jodidaarStatus);
    PointsData findFirstByStatusAndRetailerStatus(Status status, RetailerStatus retailerStatus);
}
