package com.lovetree.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lovetree.dto.CreateEventRequest;
import com.lovetree.dto.EventResponse;

import java.util.Map;

public interface EventService {

    IPage<EventResponse> list(Long coupleId, int page, int size, String eventType);

    EventResponse create(Long coupleId, Long authorId, CreateEventRequest request);

    EventResponse getById(Long id, Long coupleId);

    EventResponse update(Long id, Long coupleId, Long authorId, CreateEventRequest request);

    void delete(Long id, Long coupleId);

    Map<String, Integer> getTypeDistribution(Long coupleId);

    Map<String, Integer> getMonthlyCounts(Long coupleId);
}
