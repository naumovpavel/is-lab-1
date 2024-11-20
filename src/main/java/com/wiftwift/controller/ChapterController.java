package com.wiftwift.controller;

import com.wiftwift.entity.Coordinates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.wiftwift.entity.Chapter;
import com.wiftwift.entity.SpaceMarine;
import com.wiftwift.entity.User;
import com.wiftwift.service.ChapterService;
import com.wiftwift.service.SpaceMarineService;
import com.wiftwift.service.UserService;
import java.util.List;

@Controller
@RequestMapping("/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @Autowired
    private SpaceMarineService spaceMarineService;

    @Autowired
    private UserService userService;

    
    @GetMapping
    public String getUserChapters(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "columnName", defaultValue = "") String columnName,
            @RequestParam(name = "searchValue", defaultValue = "") String searchValue,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            Authentication authentication
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String username = authentication.getName();
        Page<Chapter> chapterPage = chapterService.getByPage(
                columnName,
                searchValue,
                PageRequest.of(page, size, Sort.by(direction, sortBy))
        );
        model.addAttribute("chapterPage", chapterPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("username", authentication.getName());
        return "chapter-list";
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Chapter> getJsonChapters(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            Authentication authentication
    ) {
        Page<Chapter> chapterPage = chapterService.getByPage(
                "",
                "",
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"))
        );
        return chapterPage.getContent();
    }

    
    @GetMapping("/{chapterId}")
    public String getSpaceMarinesByChapter(@PathVariable("chapterId") int chapterId, Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection) {
        return "redirect:/space-marines/chapter/" + chapterId + "?page=" + page + "&size=" + size + "&sortBy=" + sortBy
                + "&sortDirection=" + sortDirection;
    }

    @GetMapping("/new")
    public String showCreateChapterForm(Model model) {
        model.addAttribute("chapter", new Chapter());
        return "create-chapter";
    }

    @PostMapping("/new")
    public String createChapter(@Valid @ModelAttribute Chapter chapter, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();
        chapter.setOwner(user);
        chapterService.saveChapter(chapter);
        return "redirect:/chapters";
    }

    @GetMapping("/edit/{id}")
    public String showEditChapterForm(@PathVariable("id") int id, Model model, Authentication authentication) {
        Chapter chapter = chapterService.getChapterById(id);
        String username = authentication.getName();
        if (!chapter.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
            throw new AccessDeniedException("You do not have permission to edit this chapter");
        }

        model.addAttribute("chapter", chapter);
        model.addAttribute("username", authentication.getName());
        return "edit-chapter";
    }

    @PostMapping("/edit")
    public String updateChapter(@Valid @ModelAttribute Chapter chapter, Authentication authentication, Model model) {
        try {
            Chapter oldChapter = chapterService.getChapterById(chapter.getId());

            String username = authentication.getName();
            if (!oldChapter.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
                throw new AccessDeniedException("You do not have permission to edit this chapter");
            }

            chapter.setOwner(oldChapter.getOwner());
            chapterService.saveChapter(chapter);

            return "redirect:/chapters";
        }  catch (java.lang.NullPointerException e) {
            model.addAttribute("error", "Упс, кто-то удалил");
            return "error";
        }  catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteChapter(@PathVariable("id") int id, Authentication authentication, Model model) {
        try {
            Chapter chapter = chapterService.getChapterById(id);


            String username = authentication.getName();
            if (!chapter.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
                throw new AccessDeniedException("You do not have permission to delete this chapter");
            }

            chapterService.deleteChapter(id);

            return "redirect:/chapters";
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            model.addAttribute("error", "Не возвожно удалить, используется в других объектах");
            return "error";

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }
}
