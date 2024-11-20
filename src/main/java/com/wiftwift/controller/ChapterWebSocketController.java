package com.wiftwift.controller;

import com.wiftwift.entity.Chapter;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChapterWebSocketController {
    // Create a new Chapter
    @MessageMapping("/chapters")
    @SendTo("/topic/chapters")
    public Chapter createChapter(Chapter chapter) {
        System.out.println("sending");
        return chapter;
    }

//    // Delete an existing Chapter
//    @MessageMapping("/deleteChapter")
//    @SendTo("/topic/chapters")
//    public int deleteChapter(int chapterId) {
//        return chapterId;
//    }
}