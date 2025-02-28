package com.protectra.sp.repo;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.protectra.sp.entity.Asset;

public interface AssetRepository extends MongoRepository<Asset, String> {

    boolean existsBySiteId(String siteId);
}
