package com.team9.cinema.service;

import com.team9.cinema.model.Promotions;
import com.team9.cinema.repository.PromotionsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class PromotionsService {

    @Autowired
    private PromotionsRepository promotionsRepository;

    public Promotions createPromotion(Promotions promotion) {
        return promotionsRepository.save(promotion);
    }

    public List<Promotions> getAllPromotions() {
        return promotionsRepository.findAll();
    }

    public void deletePromotionById(Long id) {
        promotionsRepository.deleteById(id);
    }

    public Optional<Promotions> getPromotionById(Long id) {
        return promotionsRepository.findById(id);
    }

    public Optional<Promotions> findPromotionByCode(String code) {
        return promotionsRepository.findByCode(code);
    }

    public BigDecimal getDiscountForCode(String code) {
        Optional<Promotions> promotion = promotionsRepository.findByCode(code);
        return promotion.map(promo -> new BigDecimal(promo.getPercentage())).orElse(null);
    }


}
