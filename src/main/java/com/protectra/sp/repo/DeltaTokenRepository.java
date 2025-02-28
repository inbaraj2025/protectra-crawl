package com.protectra.sp.repo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.protectra.sp.entity.DeltaTokenEntity;

@Repository
public interface DeltaTokenRepository extends MongoRepository<DeltaTokenEntity, String> {

    Optional<DeltaTokenEntity> findBySiteIdAndDriveId(String siteId, String driveId);
}
