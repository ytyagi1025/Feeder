package com.lucas.feeder.dao;

import com.lucas.feeder.model.*;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PointsRepository extends MongoRepository<PointsData, String> {
    PointsData findFirstByStatusAndJodidaarStatusAndCodeType(Status status, JodidaarStatus jodidaarStatus, CodeType codeType);
    PointsData findFirstByStatusAndRetailerStatusAndCodeType(Status status, RetailerStatus retailerStatus, CodeType codeType);
}
