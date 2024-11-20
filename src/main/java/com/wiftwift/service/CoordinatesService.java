package com.wiftwift.service;

import com.wiftwift.dto.DeleteEventDto;
import com.wiftwift.entity.Coordinates;
import com.wiftwift.repository.CoordinatesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class CoordinatesService {

    @Autowired
    private CoordinatesRepository coordinatesRepository;

    @Autowired
    private SimpMessagingTemplate template;

    public Page<Coordinates> getByPage(Pageable pageable) {
        return coordinatesRepository.findAll(pageable);
    }

    public Coordinates saveCoordinates(Coordinates Coordinates) {
        var coord = coordinatesRepository.save(Coordinates);
        template.convertAndSend("/topic/coordinates", coord);
        return coord;
    }

    public Coordinates getCoordinatesById(int CoordinatesId) {
        return coordinatesRepository.findById(CoordinatesId).orElse(null);
    }

    public void deleteCoordinates(int CoordinatesId) {
        coordinatesRepository.deleteById(CoordinatesId);
        var dto = new DeleteEventDto();
        dto.setDeleteID(CoordinatesId);
        template.convertAndSend("/topic/coordinates", dto);
    }

    public List<Coordinates> getAllCoordinatess() {
        return coordinatesRepository.findAll();
    }

}
