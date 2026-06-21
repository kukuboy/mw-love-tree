package com.lovetree.service.impl;

import com.lovetree.common.BusinessException;
import com.lovetree.dto.TreeResponse;
import com.lovetree.dto.TreeStatsResponse;
import com.lovetree.entity.Tree;
import com.lovetree.mapper.TreeMapper;
import com.lovetree.service.EventService;
import com.lovetree.service.TreeService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TreeServiceImpl implements TreeService {

    private final TreeMapper treeMapper;
    private final EventService eventService;

    public TreeServiceImpl(TreeMapper treeMapper, EventService eventService) {
        this.treeMapper = treeMapper;
        this.eventService = eventService;
    }

    @Override
    public TreeResponse getTree(Long coupleId) {
        Tree tree = treeMapper.findByCoupleId(coupleId);
        if (tree == null) {
            throw new BusinessException(404, "Tree not found");
        }
        String stageLabel = getStageLabel(tree.getStage());
        return new TreeResponse(tree.getStage(), tree.getLeafCount(), tree.getFlowerCount(), stageLabel);
    }

    @Override
    public TreeStatsResponse getStats(Long coupleId) {
        Map<String, Integer> typeDistribution = eventService.getTypeDistribution(coupleId);
        Map<String, Integer> monthlyCounts = eventService.getMonthlyCounts(coupleId);

        long totalEvents = typeDistribution.values().stream().mapToLong(Integer::longValue).sum();

        return new TreeStatsResponse(totalEvents, typeDistribution, monthlyCounts);
    }

    private String getStageLabel(String stage) {
        switch (stage) {
            case "SEED":
                return "🌱 种子";
            case "SPROUT":
                return "🌿 嫩芽";
            case "SAPLING":
                return "🎴 小树";
            case "BLOOM":
                return "🌸 花树";
            case "FRUIT":
                return "🍊 果树";
            default:
                return "🌱 种子";
        }
    }
}
