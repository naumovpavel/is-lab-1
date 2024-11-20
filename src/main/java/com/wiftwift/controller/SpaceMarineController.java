package com.wiftwift.controller;

import com.wiftwift.dto.CategoryCountDto;
import com.wiftwift.dto.SetChapterDto;
import com.wiftwift.entity.*;
import com.wiftwift.service.CoordinatesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.wiftwift.service.ChapterService;
import com.wiftwift.service.SpaceMarineService;
import com.wiftwift.service.UserService;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/space-marines")
public class SpaceMarineController {

    @Autowired
    private SpaceMarineService spaceMarineService;

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private CoordinatesService coordinatesService;

    @Autowired
    private UserService userService;

    
    @GetMapping
    public String getAllSpaceMarines(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "columnName", defaultValue = "") String columnName,
            @RequestParam(name = "searchValue", defaultValue = "") String searchValue,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            Model model,
            Authentication authentication
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<SpaceMarine> spaceMarinePage = spaceMarineService.getAllSpaceMarines(
                columnName,
                searchValue,
                PageRequest.of(page, size, Sort.by(direction, sortBy))
        );
        model.addAttribute("spaceMarinePage", spaceMarinePage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("username", authentication.getName());
        return "space-marine-list";
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<SpaceMarine> getJson(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication authentication
    ) {
        Page<SpaceMarine> spaceMarinePage = spaceMarineService.getAllSpaceMarines(
                "",
                "",
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
        return spaceMarinePage.getContent();
    }

    
    @GetMapping("/chapter/{chapterId}")
    public String getSpaceMarinesByChapter(
            @PathVariable("chapterId") int chapterId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            Model model,
            Authentication authentication
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<SpaceMarine> spaceMarinePage = spaceMarineService.getSpaceMarinesByChapter(chapterId,
                PageRequest.of(page, size, Sort.by(direction, sortBy)));
        model.addAttribute("spaceMarinePage", spaceMarinePage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("username", authentication.getName());
        return "space-marine-list";
    }

    @GetMapping("/add")
    public String showAddSpaceMarineForm(
            Model model,
            @RequestParam(name = "chapters_page", defaultValue = "0") int chaptersPage,
            @RequestParam(name = "coordinates_page", defaultValue = "0") int coordinatesPage,
            Authentication authentication
    ) {
        Page<Chapter> chapterPage = chapterService.getByPage(
                "",
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );
        Page<Coordinates> coordinates = coordinatesService.getByPage(PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "x")));
        model.addAttribute("chapters", chapterPage.getContent());
        model.addAttribute("coordinates", coordinates.getContent());
        model.addAttribute("spaceMarine", new SpaceMarine());
        model.addAttribute("username", authentication.getName());
        return "add-space-marine";
    }

    
    @PostMapping("/add")
    public String addSpaceMarine(@ModelAttribute SpaceMarine spaceMarine, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();
        spaceMarine.setOwner(user);
        spaceMarineService.saveSpaceMarine(spaceMarine);
        return "redirect:/space-marines"; 
    }

    
    @GetMapping("/edit/{id}")
    public String showEditSpaceMarineForm(@PathVariable("id") int id, Model model, Authentication authentication) {
        SpaceMarine spaceMarine = spaceMarineService.getSpaceMarineById(id);
        List<Coordinates> coordinates = coordinatesService.getAllCoordinatess();
        List<Chapter> chapters = chapterService.getAllChapters();
        model.addAttribute("coordinates", coordinates);
        model.addAttribute("chapters", chapters);
        model.addAttribute("spaceMarine", spaceMarine);
        model.addAttribute("username", authentication.getName());
        return "edit-space-marine";
    }

    
    @PostMapping("/edit")
    public String updateSpaceMarine(@ModelAttribute SpaceMarine spaceMarine, Authentication authentication, Model model) {
        try {

            SpaceMarine oldSpaceMarine = spaceMarineService.getSpaceMarineById(spaceMarine.getId());

            String username = authentication.getName();
            if (!oldSpaceMarine.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
                throw new AccessDeniedException("You do not have permission to edit this chapter");
            }

            spaceMarine.setOwner(oldSpaceMarine.getOwner());

            spaceMarineService.saveSpaceMarine(spaceMarine);
            return "redirect:/space-marines";
        }  catch (java.lang.NullPointerException e) {
            model.addAttribute("error", "Упс, кто-то удалил");
            return "error";
        }  catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @GetMapping("/setChapter/{id}")
    public String showSetChapter(@PathVariable("id") int id, Model model, Authentication authentication) {
        Page<SpaceMarine> spaceMarine = spaceMarineService.getAllSpaceMarines(
                "",
                "",
                PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"))
        );
        Chapter chapter = chapterService.getChapterById(id);

        model.addAttribute("chapter", chapter);
        model.addAttribute("spaceMarines", spaceMarine.getContent());
        model.addAttribute("username", authentication.getName());

        return "set-chapter";
    }

    @PostMapping(value = "/setChapter", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String setChapter(
            @RequestParam(name = "spaceMarineID") int spaceMarineID,
            @RequestParam(name = "chapterID") int chapterID,
            Authentication authentication
    ) {
        SpaceMarine spaceMarine = spaceMarineService.getSpaceMarineById(spaceMarineID);
        Chapter chapter = chapterService.getChapterById(chapterID);

        String username = authentication.getName();
        if (!spaceMarine.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this chapter");
        }

        spaceMarine.setChapter(chapter);

        spaceMarineService.saveSpaceMarine(spaceMarine);
        return "redirect:/chapters";
    }

    @PostMapping("/unsetChapter/{id}")
    public String unsetChapter(
            @PathVariable("id") int id,
            Authentication authentication
    ) {
        SpaceMarine spaceMarine = spaceMarineService.getSpaceMarineById(id);
        spaceMarine.setChapter(null);

        String username = authentication.getName();
        if (!spaceMarine.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this chapter");
        }

        spaceMarineService.saveSpaceMarine(spaceMarine);
        return "redirect:/space-marines";
    }

    
    @PostMapping("/delete/{id}")
    public String deleteSpaceMarine(@PathVariable("id") int id) {
        spaceMarineService.deleteSpaceMarine(id); 
        return "redirect:/space-marines"; 
    }

    @GetMapping("/uniqueHealth")
    public String uniqueHealth(
            Model model,
            Authentication authentication
    ) {
        var unique = spaceMarineService.getUniqueHealthValues();
        model.addAttribute("uniqueHealth", unique);
        model.addAttribute("username", authentication.getName());
        return "uniq";
    }

    @GetMapping("/avgHealth")
    public String avgHealth(
            Model model,
            Authentication authentication
    ) {
        var avg = spaceMarineService.getAverageHealth();
        model.addAttribute("avg", avg);
        model.addAttribute("username", authentication.getName());

        return "avg-health";
    }

    @GetMapping("/categoryCount")
    public String categoryCount(
            Model model,
            Authentication authentication
    ) {
        var spaceMarines = spaceMarineService.getAllSpaceMarines("","", Pageable.unpaged());
        var categoryMap = new HashMap<AstartesCategory, Long>();
        for (var spaceMarine : spaceMarines) {
            if (categoryMap.get(spaceMarine.getCategory()) != null) {
                categoryMap.put(spaceMarine.getCategory(), categoryMap.get(spaceMarine.getCategory()) + 1);
            } else {
                categoryMap.put(spaceMarine.getCategory(), 1L);
            }
        }


        var dtos = new LinkedList<CategoryCountDto>();
        for (var k : categoryMap.keySet()) {
            dtos.addLast(new CategoryCountDto(k, categoryMap.get(k)));
        }

        model.addAttribute("username", authentication.getName());
        model.addAttribute("categories", dtos);
        return "category";
    }
}
