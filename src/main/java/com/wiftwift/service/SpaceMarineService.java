package com.wiftwift.service;

import com.wiftwift.dto.CategoryCountDto;
import com.wiftwift.dto.DeleteEventDto;
import com.wiftwift.entity.Chapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.wiftwift.entity.SpaceMarine;
import com.wiftwift.repository.SpaceMarineRepository;

import java.util.List;

@Service
public class SpaceMarineService {

    @Autowired
    private SpaceMarineRepository spaceMarineRepository;

    @Autowired
    private SimpMessagingTemplate template;

    public Page<SpaceMarine> getAllSpaceMarines(String columnName, String searchValue, Pageable pageable) {
        if (columnName.equals("name")) {
            return spaceMarineRepository.findByNameContaining(searchValue, pageable);
        }
        return spaceMarineRepository.findAll(pageable);
    }

    public Page<SpaceMarine> getSpaceMarinesByChapter(int chapterId, Pageable pageable) {
        return spaceMarineRepository.findByChapter_Id(chapterId, pageable);
    }

    public List<SpaceMarine> getSpaceMarineByChapter(int chapterId) {
        return spaceMarineRepository.findByChapter_Id(chapterId);
    }

    public List<SpaceMarine> getSpaceMarineByCoordinate(int coordinateId) {
        return spaceMarineRepository.findByCoordinates_Id(coordinateId);
    }

    public SpaceMarine saveSpaceMarine(SpaceMarine spaceMarine) {
        var savedSpaceMarine = spaceMarineRepository.save(spaceMarine);
        template.convertAndSend("/topic/space-marines", savedSpaceMarine);
        return savedSpaceMarine;
    }

    public SpaceMarine getSpaceMarineById(int spaceMarineId) {
        return spaceMarineRepository.findById(spaceMarineId).orElse(null);
    }

    public void deleteSpaceMarine(int spaceMarineId) {
        spaceMarineRepository.deleteById(spaceMarineId);
        var dto = new DeleteEventDto();
        dto.setDeleteID(spaceMarineId);
        template.convertAndSend("/topic/space-marines", dto);
    }

    public Double getAverageHealth() {
        return spaceMarineRepository.calculateAverageHealth();
    }

    public List<Long> getUniqueHealthValues() {
        return spaceMarineRepository.findUniqueHealthValues();
    }

}
