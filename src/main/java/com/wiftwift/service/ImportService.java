package com.wiftwift.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.wiftwift.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Service
public class ImportService {
    @Autowired
    private ChapterService chapterService;

    @Autowired
    private SpaceMarineService spaceMarineService;

    @Autowired
    private UserService userService;

    @Autowired
    private ImportAttemptService importAttemptService;

    @Autowired
    private MinioService minioService;

    private final XmlMapper xmlMapper = new XmlMapper();

    @Transactional(isolation = Isolation.SERIALIZABLE, rollbackFor = Exception.class)
    public void importFromFile(MultipartFile file, String username, String fileName) throws Exception {
        executeImport(file, username, fileName);
        System.out.println("ДО");
        Thread.sleep(10 * 1000);
        System.out.println("После");
    }

    public void executeImport(MultipartFile file, String username, String fileName ) throws Exception {
        ImportAttempt attempt = new ImportAttempt();
        attempt.setOwner(userService.findByUsername(username).orElseThrow());
        attempt.setNewObjectsCounter(0);
        attempt.setFilename(fileName);
        try {
            JsonNode rootNode = xmlMapper.readTree(file.getInputStream());

            List<Chapter> parsedChapters = new ArrayList<>();
            if (rootNode.has("chapters")) {
                JsonNode chaptersNode = rootNode.get("chapters");
                if (chaptersNode.isArray()) {
                    for (JsonNode chapterNode : chaptersNode) {
                        Chapter chapter = parseChapter(chapterNode);
                        parsedChapters.add(chapter);
                    }
                } else if (chaptersNode.has("chapter")) {
                    JsonNode chapterSubNode = chaptersNode.get("chapter");
                    if (chapterSubNode.isArray()) {
                        for (JsonNode chapterNode : chapterSubNode) {
                            Chapter chapter = parseChapter(chapterNode);
                            parsedChapters.add(chapter);
                        }
                    } else {
                        Chapter chapter = parseChapter(chapterSubNode);
                        parsedChapters.add(chapter);
                    }
                }
            }

            List<SpaceMarine> parsedMarines = new ArrayList<>();
            if (rootNode.has("spaceMarines")) {
                JsonNode marinesNode = rootNode.get("spaceMarines");
                if (marinesNode.isArray()) {
                    for (JsonNode marineNode : marinesNode) {
                        SpaceMarine marine = parseSpaceMarine(marineNode, parsedChapters);
                        parsedMarines.add(marine);
                    }
                } else if (marinesNode.has("spaceMarine")) {
                    JsonNode marineSubNode = marinesNode.get("spaceMarine");
                    if (marineSubNode.isArray()) {
                        for (JsonNode marineNode : marineSubNode) {
                            SpaceMarine marine = parseSpaceMarine(marineNode, parsedChapters);
                            parsedMarines.add(marine);
                        }
                    } else {
                        SpaceMarine marine = parseSpaceMarine(marineSubNode, parsedChapters);
                        parsedMarines.add(marine);
                    }
                }
            }

            for (Chapter chapter : parsedChapters) {
                chapter.setOwner(attempt.getOwner());
            }
            for (SpaceMarine marine : parsedMarines) {
                marine.setOwner(attempt.getOwner());
                if (marine.getCoordinates() != null) {
                    marine.getCoordinates().setOwner(attempt.getOwner());
                }
            }
            chapterService.saveChapters(parsedChapters);
            attempt.setNewObjectsCounter(attempt.getNewObjectsCounter() + parsedChapters.size());
            spaceMarineService.saveSpaceMarines(parsedMarines);
            attempt.setNewObjectsCounter(attempt.getNewObjectsCounter() + parsedMarines.size());
            minioService.uploadFile(file, fileName);
            // throw new RuntimeException("Ooopps");
            attempt.setAccepted(true);
            importAttemptService.save(attempt);
        } catch (Exception e) {
            System.out.println("Expection minio");
            attempt.setAccepted(false);
            attempt.setNewObjectsCounter(0);
            importAttemptService.save(attempt);
            minioService.deleteFile(fileName);
            throw e;
        }

    }

    private Chapter parseChapter(JsonNode node) {
        Chapter chapter = new Chapter();
        if (node.has("name")) {
            chapter.setName(node.get("name").asText());
        }
        if (node.has("world")) {
            chapter.setWorld(node.get("world").asText());
        }
        return chapter;
    }

    private SpaceMarine parseSpaceMarine(JsonNode node, List<Chapter> availableChapters) {
        SpaceMarine marine = new SpaceMarine();
        if (node.has("name")) {
            marine.setName(node.get("name").asText());
        }
        if (node.has("coordinates")) {
            JsonNode coordNode = node.get("coordinates");
            Coordinates coordinates = new Coordinates();
            if (coordNode.has("x")) {
                coordinates.setX(coordNode.get("x").asInt());
            }
            if (coordNode.has("y")) {
                coordinates.setY(coordNode.get("y").asDouble());
            }
            marine.setCoordinates(coordinates);
        }
        if (node.has("category")) {
            marine.setCategory(AstartesCategory.valueOf(node.get("category").asText()));
        }
        if (node.has("weaponType")) {
            marine.setWeaponType(Weapon.valueOf(node.get("weaponType").asText()));
        }
        if (node.has("health")) {
            marine.setHealth(node.get("health").asLong());
        }
        if (node.has("height")) {
            marine.setHeight(node.get("height").asInt());
        }
        if (node.has("chapter")) {
            String chapterName = node.get("chapter").asText();
            marine.setChapter(availableChapters.stream()
                    .filter(c -> c.getName().equals(chapterName))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Chapter not found: " + chapterName)));
        }
        return marine;
    }


    private void cleanupOnError(String tempFileName) {
        try {
            minioService.deleteFile(tempFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
