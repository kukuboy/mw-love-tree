package com.lovetree.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovetree.common.BusinessException;
import com.lovetree.dto.CreateEventRequest;
import com.lovetree.dto.EventResponse;
import com.lovetree.entity.LoveEvent;
import com.lovetree.entity.Tree;
import com.lovetree.entity.User;
import com.lovetree.mapper.LoveEventMapper;
import com.lovetree.mapper.TreeMapper;
import com.lovetree.mapper.UserMapper;
import com.lovetree.service.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EventServiceImpl implements EventService {

    private static final Set<String> VALID_EVENT_TYPES = Set.of(
            "first_meet", "first_date", "first_kiss", "proposal", "travel",
            "anniversary", "birthday", "valentine", "christmas", "new_year",
            "daily", "other"
    );

    private static final Set<String> BLOSSOM_TYPES = Set.of(
            "first_meet", "first_date", "first_kiss", "proposal",
            "anniversary", "birthday", "valentine", "christmas", "new_year"
    );

    private final LoveEventMapper loveEventMapper;
    private final TreeMapper treeMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public EventServiceImpl(LoveEventMapper loveEventMapper, TreeMapper treeMapper,
                            UserMapper userMapper, ObjectMapper objectMapper) {
        this.loveEventMapper = loveEventMapper;
        this.treeMapper = treeMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public IPage<EventResponse> list(Long coupleId, int page, int size, String eventType) {
        QueryWrapper<LoveEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("couple_id", coupleId);
        if (eventType != null && !eventType.isEmpty()) {
            queryWrapper.eq("event_type", eventType);
        }
        queryWrapper.orderByDesc("event_date");

        Page<LoveEvent> pageParam = new Page<>(page, size);
        IPage<LoveEvent> eventPage = loveEventMapper.selectPage(pageParam, queryWrapper);

        List<LoveEvent> records = eventPage.getRecords();

        // Batch fetch author nicknames
        Map<Long, String> nicknameMap = fetchNicknames(records);

        // Convert to EventResponse list
        List<EventResponse> responseList = records.stream()
                .map(event -> toEventResponse(event, nicknameMap.get(event.getAuthorId())))
                .collect(Collectors.toList());

        // Build new page with EventResponse
        Page<EventResponse> responsePage = new Page<>(pageParam.getCurrent(), pageParam.getSize(), eventPage.getTotal());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    @Override
    @Transactional
    public EventResponse create(Long coupleId, Long authorId, CreateEventRequest request) {
        // Validate eventType
        if (!VALID_EVENT_TYPES.contains(request.getEventType())) {
            throw new BusinessException(400, "Invalid event type: " + request.getEventType());
        }

        // Build event entity
        LoveEvent event = new LoveEvent();
        event.setCoupleId(coupleId);
        event.setAuthorId(authorId);
        event.setTitle(request.getTitle());
        event.setContent(request.getContent());
        event.setEventType(request.getEventType());
        event.setEventDate(request.getEventDate());
        event.setMood(request.getMood());

        // Serialize images list to JSON string
        try {
            String imagesJson = objectMapper.writeValueAsString(
                    request.getImages() != null ? request.getImages() : new ArrayList<>());
            event.setImages(imagesJson);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize images");
        }

        // Insert event
        loveEventMapper.insert(event);

        // Update tree
        Tree tree = treeMapper.findByCoupleId(coupleId);
        if (tree == null) {
            throw new BusinessException(404, "Tree not found");
        }

        tree.setLeafCount(tree.getLeafCount() + 1);
        if (BLOSSOM_TYPES.contains(request.getEventType())) {
            tree.setFlowerCount(tree.getFlowerCount() + 1);
        }
        tree.setStage(determineStage(tree.getLeafCount()));
        treeMapper.updateById(tree);

        // Return response
        User author = userMapper.selectById(authorId);
        String nickname = author != null ? author.getNickname() : null;
        return toEventResponse(event, nickname);
    }

    @Override
    public EventResponse getById(Long id, Long coupleId) {
        LoveEvent event = loveEventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException(404, "Event not found");
        }
        if (!event.getCoupleId().equals(coupleId)) {
            throw new BusinessException(403, "Access denied: event does not belong to this couple");
        }

        User author = userMapper.selectById(event.getAuthorId());
        String nickname = author != null ? author.getNickname() : null;
        return toEventResponse(event, nickname);
    }

    @Override
    @Transactional
    public EventResponse update(Long id, Long coupleId, Long authorId, CreateEventRequest request) {
        LoveEvent event = loveEventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException(404, "Event not found");
        }
        if (!event.getCoupleId().equals(coupleId)) {
            throw new BusinessException(403, "Access denied: event does not belong to this couple");
        }
        if (!event.getAuthorId().equals(authorId)) {
            throw new BusinessException(403, "Access denied: not the author of this event");
        }

        // Update fields
        event.setTitle(request.getTitle());
        event.setContent(request.getContent());
        event.setEventType(request.getEventType());
        event.setEventDate(request.getEventDate());
        event.setMood(request.getMood());

        try {
            String imagesJson = objectMapper.writeValueAsString(
                    request.getImages() != null ? request.getImages() : new ArrayList<>());
            event.setImages(imagesJson);
        } catch (Exception e) {
            throw new BusinessException("Failed to serialize images");
        }

        loveEventMapper.updateById(event);

        User author = userMapper.selectById(authorId);
        String nickname = author != null ? author.getNickname() : null;
        return toEventResponse(event, nickname);
    }

    @Override
    @Transactional
    public void delete(Long id, Long coupleId) {
        LoveEvent event = loveEventMapper.selectById(id);
        if (event == null) {
            throw new BusinessException(404, "Event not found");
        }
        if (!event.getCoupleId().equals(coupleId)) {
            throw new BusinessException(403, "Access denied: event does not belong to this couple");
        }

        loveEventMapper.deleteById(id);

        // Update tree
        Tree tree = treeMapper.findByCoupleId(coupleId);
        if (tree != null) {
            tree.setLeafCount(Math.max(0, tree.getLeafCount() - 1));
            if (BLOSSOM_TYPES.contains(event.getEventType())) {
                tree.setFlowerCount(Math.max(0, tree.getFlowerCount() - 1));
            }
            tree.setStage(determineStage(tree.getLeafCount()));
            treeMapper.updateById(tree);
        }
    }

    @Override
    public Map<String, Integer> getTypeDistribution(Long coupleId) {
        List<Map<String, Object>> rows = loveEventMapper.countByType(coupleId);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put((String) row.get("eventType"), ((Number) row.get("cnt")).intValue());
        }
        return result;
    }

    @Override
    public Map<String, Integer> getMonthlyCounts(Long coupleId) {
        List<Map<String, Object>> rows = loveEventMapper.countByMonth(coupleId);
        Map<String, Integer> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            result.put((String) row.get("month"), ((Number) row.get("cnt")).intValue());
        }
        return result;
    }

    private String determineStage(int leafCount) {
        if (leafCount <= 0) return "SEED";
        if (leafCount <= 5) return "SPROUT";
        if (leafCount <= 15) return "SAPLING";
        if (leafCount <= 30) return "BLOOM";
        return "FRUIT";
    }

    private EventResponse toEventResponse(LoveEvent event, String authorNickname) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setCoupleId(event.getCoupleId());
        response.setAuthorId(event.getAuthorId());
        response.setAuthorNickname(authorNickname);
        response.setTitle(event.getTitle());
        response.setContent(event.getContent());
        response.setEventType(event.getEventType());
        response.setEventDate(event.getEventDate());
        response.setMood(event.getMood());
        response.setCreatedAt(event.getCreatedAt());

        // Parse images JSON string to List<String>
        if (event.getImages() != null && !event.getImages().isEmpty()) {
            try {
                List<String> imagesList = objectMapper.readValue(event.getImages(),
                        new TypeReference<List<String>>() {});
                response.setImages(imagesList);
            } catch (Exception e) {
                response.setImages(new ArrayList<>());
            }
        } else {
            response.setImages(new ArrayList<>());
        }

        return response;
    }

    private Map<Long, String> fetchNicknames(List<LoveEvent> events) {
        List<Long> authorIds = events.stream()
                .map(LoveEvent::getAuthorId)
                .distinct()
                .collect(Collectors.toList());

        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userMapper.selectBatchIds(authorIds);
        return users.stream().collect(Collectors.toMap(User::getId, User::getNickname));
    }
}
