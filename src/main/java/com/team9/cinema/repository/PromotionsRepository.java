package com.team9.cinema.repository;

import com.team9.cinema.model.Promotions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PromotionsRepository extends JpaRepository<Promotions, Long> {
    Optional<Promotions> findByCode(String code);
}
