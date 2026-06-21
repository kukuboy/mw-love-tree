package com.lovetree.service;

import com.lovetree.dto.TreeResponse;
import com.lovetree.dto.TreeStatsResponse;

public interface TreeService {

    TreeResponse getTree(Long coupleId);

    TreeStatsResponse getStats(Long coupleId);
}
