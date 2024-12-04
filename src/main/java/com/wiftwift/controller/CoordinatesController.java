package com.wiftwift.controller;

import com.wiftwift.entity.Coordinates;
import com.wiftwift.entity.User;
import com.wiftwift.service.CoordinatesService;
import com.wiftwift.service.SpaceMarineService;
import com.wiftwift.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/coordinates")
public class CoordinatesController {

    @Autowired
    private CoordinatesService coordinatesService;

    @Autowired
    private SpaceMarineService spaceMarineService;

    @Autowired
    private UserService userService;

    
    @GetMapping
    public String getUserCoordinates(
            Model model,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "x") String sortBy,
            @RequestParam(name = "sortDirection", defaultValue = "asc") String sortDirection,
            Authentication authentication
    ) {
        Sort.Direction direction = sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Page<Coordinates> coordinatesPage = coordinatesService.getByPage(PageRequest.of(page, size, Sort.by(direction, sortBy)));
        model.addAttribute("coordinatesPage", coordinatesPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDirection", sortDirection);
        model.addAttribute("username", authentication.getName());
        return "coordinates-list";
    }

    @GetMapping(value = "/json", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Coordinates> getJsonCoordinates(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) {
        Page<Coordinates> coordinatesPage = coordinatesService.getByPage(
                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "x"))
        );
        return coordinatesPage.getContent();
    }

    @GetMapping("/new")
    public String showCreateCoordinatesForm(Model model, Authentication authentication) {
        model.addAttribute("coordinate", new Coordinates());
        model.addAttribute("username", authentication.getName());

        return "create-coordinate";
    }

    @Transactional
    @PostMapping("/new")
    public String createCoordinates(@Valid @ModelAttribute Coordinates coordinates, Authentication authentication) {
        String username = authentication.getName();
        User user = userService.findByUsername(username).orElseThrow();
        coordinates.setOwner(user);
        coordinatesService.saveCoordinates(coordinates);
        return "redirect:/coordinates";
    }

    @GetMapping("/edit/{id}")
    public String showEditCoordinatesForm(@PathVariable("id") int id, Model model, Authentication authentication) {
        Coordinates coordinates = coordinatesService.getCoordinatesById(id);

        String username = authentication.getName();
        if (!coordinates.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
            model.addAttribute("error", "У вас нет прав!");
            return "error";
        }

        model.addAttribute("coordinate", coordinates);
        model.addAttribute("username", authentication.getName());

        return "edit-coordinate";
    }

    @Transactional
    @PostMapping("/edit")
    public String updateCoordinates(@Valid @ModelAttribute Coordinates coordinates, Authentication authentication, Model model) {
        try {
            Coordinates oldCoordinates = coordinatesService.getCoordinatesById(coordinates.getId());

            String username = authentication.getName();
            if (!oldCoordinates.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
                throw new AccessDeniedException("You do not have permission to edit this Coordinates");
            }

            User user = userService.findByUsername(username).orElseThrow();
            coordinates.setOwner(user);

            coordinatesService.saveCoordinates(coordinates);

            return "redirect:/coordinates";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "У вас нет прав!");
            return "error";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Не возвожно удалить, используется в других объектах");
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Что то пошло не так. Пожалуйста, попробуйте еще раз позже.");
            return "error";
        }
    }

    @Transactional
    @PostMapping("/delete/{id}")
    public String deleteCoordinates(@PathVariable("id") int id, Authentication authentication, Model model) {
        try {
            Coordinates coordinates = coordinatesService.getCoordinatesById(id);

            String username = authentication.getName();
            if (!coordinates.getOwner().getUsername().equals(username) && !userService.isAdmin(authentication.getName())) {
                throw new AccessDeniedException("You do not have permission to delete this Coordinates");
            }

            coordinatesService.deleteCoordinates(id);

            return "redirect:/coordinates";
        } catch (AccessDeniedException e) {
            model.addAttribute("error", "У вас нет прав!");
            return "error";
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Не возвожно удалить, используется в других объектах");
            return "error";
        } catch (Exception e) {
            model.addAttribute("error", "Что то пошло не так. Пожалуйста, попробуйте еще раз позже.");
            return "error";
        }
    }
}
