package com.lovetree.service;

import com.lovetree.dto.SpecialDayResponse;

import java.util.List;

public interface SpecialDayService {
    List<SpecialDayResponse> getSpecialDays(Long coupleId);
}
