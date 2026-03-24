package com.tp.aiplanner.api;

import java.util.concurrent.CompletableFuture;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessagesController {

    @GetMapping
    public ResponseEntity<String[]> list() {
        return ResponseEntity.ok(new String[] {
            "Hello, World!",
            "Welcome to AI Planner API.",
            "This is a sample message."
        });
    }
}

