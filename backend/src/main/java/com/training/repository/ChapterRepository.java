package com.training.repository;

import com.training.model.Chapter;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class ChapterRepository {

    private final Map<Long, Chapter> chapters = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public Chapter save(Chapter chapter) {
        if (chapter.getId() == null) {
            chapter.setId(idCounter.getAndIncrement());
            chapter.setCreatedAt(LocalDateTime.now());
        }
        chapter.setUpdatedAt(LocalDateTime.now());
        chapters.put(chapter.getId(), chapter);
        return chapter;
    }

    public Optional<Chapter> findById(Long id) {
        return Optional.ofNullable(chapters.get(id));
    }

    public List<Chapter> findAll() {
        return new ArrayList<>(chapters.values());
    }

    public List<Chapter> findByCourseId(Long courseId) {
        return chapters.values().stream()
                .filter(c -> courseId.equals(c.getCourseId()))
                .sorted(Comparator.comparingInt(Chapter::getChapterOrder))
                .collect(Collectors.toList());
    }

    public List<Chapter> findMandatoryChaptersByCourseId(Long courseId) {
        return chapters.values().stream()
                .filter(c -> courseId.equals(c.getCourseId()))
                .filter(Chapter::isMandatory)
                .sorted(Comparator.comparingInt(Chapter::getChapterOrder))
                .collect(Collectors.toList());
    }

    public void deleteById(Long id) {
        chapters.remove(id);
    }

    public void deleteByCourseId(Long courseId) {
        chapters.entrySet().removeIf(entry -> 
            courseId.equals(entry.getValue().getCourseId()));
    }

    public boolean existsById(Long id) {
        return chapters.containsKey(id);
    }
}
