package com.lovetree.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lovetree.dto.SpecialDayResponse;
import com.lovetree.entity.Couple;
import com.lovetree.entity.LoveEvent;
import com.lovetree.mapper.CoupleMapper;
import com.lovetree.mapper.LoveEventMapper;
import com.lovetree.service.SpecialDayService;
import org.springframework.stereotype.Service;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class SpecialDayServiceImpl implements SpecialDayService {

    private final CoupleMapper coupleMapper;
    private final LoveEventMapper loveEventMapper;

    public SpecialDayServiceImpl(CoupleMapper coupleMapper, LoveEventMapper loveEventMapper) {
        this.coupleMapper = coupleMapper;
        this.loveEventMapper = loveEventMapper;
    }

    @Override
    public List<SpecialDayResponse> getSpecialDays(Long coupleId) {
        Couple couple = coupleMapper.selectById(coupleId);
        if (couple == null) {
            return new ArrayList<>();
        }

        // Use togetherDate if set, otherwise fall back to anniversary
        LocalDate effectiveDate = couple.getTogetherDate() != null ? couple.getTogetherDate() : couple.getAnniversary();
        if (effectiveDate == null) {
            return new ArrayList<>();
        }

        LocalDate today = LocalDate.now();

        // Compute days together from effective date to today
        long daysTogether = ChronoUnit.DAYS.between(effectiveDate, today);

        // Find next anniversary date (same month/day as effective date, in current or next year)
        LocalDate nextAnniversary = nextOccurrence(effectiveDate, today);
        int daysUntil = (int) ChronoUnit.DAYS.between(today, nextAnniversary);

        List<SpecialDayResponse> days = new ArrayList<>();
        days.add(new SpecialDayResponse("在一起纪念日", nextAnniversary, daysUntil, daysTogether));

        // Query events with type='anniversary' as additional special days
        QueryWrapper<LoveEvent> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("couple_id", coupleId)
                    .eq("event_type", "anniversary");
        List<LoveEvent> anniversaryEvents = loveEventMapper.selectList(queryWrapper);

        if (anniversaryEvents != null) {
            for (LoveEvent event : anniversaryEvents) {
                LocalDate eventDate = event.getEventDate();
                if (eventDate == null) continue;

                long eventDaysTogether = ChronoUnit.DAYS.between(eventDate, today);

                // Compute next occurrence of this event's date
                LocalDate nextEventDate = nextOccurrence(eventDate, today);
                int eventDaysUntil = (int) ChronoUnit.DAYS.between(today, nextEventDate);

                // Avoid duplicate entries for the same date as the main anniversary
                if (!nextEventDate.isEqual(nextAnniversary) || !eventDate.isEqual(effectiveDate)) {
                    days.add(new SpecialDayResponse(event.getTitle(), nextEventDate, eventDaysUntil, eventDaysTogether));
                }
            }
        }

        // Sort by daysUntil ascending
        days.sort(Comparator.comparingInt(SpecialDayResponse::getDaysUntil));

        return days;
    }

    /**
     * Returns the next occurrence of the given reference date's month/day
     * relative to today (today or later).
     */
    private static LocalDate nextOccurrence(LocalDate reference, LocalDate today) {
        try {
            LocalDate candidate = LocalDate.of(today.getYear(), reference.getMonthValue(), reference.getDayOfMonth());
            if (!candidate.isAfter(today)) {
                candidate = LocalDate.of(today.getYear() + 1, reference.getMonthValue(), reference.getDayOfMonth());
            }
            return candidate;
        } catch (DateTimeException e) {
            // Handle Feb 29 on non-leap years by using Feb 28
            try {
                // Try with month's actual max day (e.g., Feb 28 in non-leap years)
                LocalDate adjusted = reference.withDayOfMonth(
                        reference.getMonth().length(today.isLeapYear() && reference.getMonthValue() == 2));
                LocalDate candidate = LocalDate.of(today.getYear(), adjusted.getMonthValue(), adjusted.getDayOfMonth());
                if (!candidate.isAfter(today)) {
                    candidate = candidate.plusYears(1);
                }
                return candidate;
            } catch (DateTimeException ex) {
                // Fallback: reference date itself
                return reference;
            }
        }
    }
}
