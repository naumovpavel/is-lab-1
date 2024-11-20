package com.wiftwift.service;

import com.wiftwift.dto.DeleteEventDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.wiftwift.entity.Chapter;
import com.wiftwift.repository.ChapterRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;


@Service
public class ChapterService {

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private SimpMessagingTemplate template;

    public Page<Chapter> getByPage(String columnName, String searchValue, Pageable pageable) {
        return switch (columnName) {
            case "name" -> chapterRepository.findByNameContaining(searchValue, pageable);
            case "world" -> chapterRepository.findByWorldContaining(searchValue, pageable);
            default -> chapterRepository.findAll(pageable);
        };
    }

    public Chapter saveChapter(Chapter chapter) {
        var savedChapter = chapterRepository.save(chapter);
        System.out.println("saved, now sending");
        template.convertAndSend("/topic/chapters", savedChapter);
        return savedChapter;
    }

    public Chapter getChapterById(int chapterId) {
        return chapterRepository.findById(chapterId).orElse(null); 
    }

    public void deleteChapter(int chapterId) {
        chapterRepository.deleteById(chapterId);
        var dto = new DeleteEventDto();
        dto.setDeleteID(chapterId);
        template.convertAndSend("/topic/chapters", dto);
    }

    public List<Chapter> getAllChapters() {
        return chapterRepository.findAll();
    }

}
