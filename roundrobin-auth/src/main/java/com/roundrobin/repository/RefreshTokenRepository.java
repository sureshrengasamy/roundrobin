package com.roundrobin.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.roundrobin.domain.RefreshToken;


@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {

  public RefreshToken findByTokenId(String tokenId);
}
