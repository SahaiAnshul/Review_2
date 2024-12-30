package com.musicstreaming.controller;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/songs")
public class SongController {

    private final SongService songService;

    public SongController(SongService songService) {
        this.songService = songService;
    }

    @GetMapping
    public List<Song> getAllSongs() {
        return songService.getAllSongs();
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> streamSong(@PathVariable int id) {
        Song song = songService.getSongById(id);
        Path path = Paths.get(song.getFilePath());
        Resource resource = new FileSystemResource(path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .body(resource);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadSong(
            @RequestParam("title") String title,
            @RequestParam("artist") String artist,
            @RequestParam("duration") double duration,
            @RequestParam("file") MultipartFile file) {
        try {
            String filePath = "music/" + file.getOriginalFilename();
            file.transferTo(new File(filePath));

            songService.saveSong(new Song(title, artist, duration, filePath));
            return ResponseEntity.ok("Song uploaded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading song: " + e.getMessage());
        }
    }
}
