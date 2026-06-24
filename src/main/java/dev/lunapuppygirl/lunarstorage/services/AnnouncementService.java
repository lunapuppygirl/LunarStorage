package dev.lunapuppygirl.lunarstorage.services;

import dev.lunapuppygirl.lunarstorage.managers.JsonFileManager;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class AnnouncementService {
    private final JsonFileManager jsonFileManager;
    private Announcement currentAnnouncement;

    public AnnouncementService(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    public void setCurrentAnnouncement(Level level, String title, String content, boolean visible) {
        setCurrentAnnouncement(new Announcement(level, title, content, visible));
    }

    public void setCurrentAnnouncement(Announcement announcement) {
        this.currentAnnouncement = announcement;

        jsonFileManager.setBoolean("announcements.visible", announcement.isVisible(), jsonFileManager.getConfigFile());
        jsonFileManager.setString("announcements.level", announcement.getLevel().name(), jsonFileManager.getConfigFile());
        jsonFileManager.setString("announcements.title", announcement.getTitle(), jsonFileManager.getConfigFile());
        jsonFileManager.setString("announcements.content", announcement.getContent(), jsonFileManager.getConfigFile());
    }

    public Announcement getCurrentAnnouncement() {
        if (currentAnnouncement == null) {
            this.currentAnnouncement = new Announcement(
                    Level.getLevel(jsonFileManager.getString("announcements.level", jsonFileManager.getConfigFile(), "LOW")),
                    jsonFileManager.getString("announcements.title", jsonFileManager.getConfigFile(), ""),
                    jsonFileManager.getString("announcements.content", jsonFileManager.getConfigFile(), ""),
                    jsonFileManager.getBoolean("announcements.visible", jsonFileManager.getConfigFile(), false)
            );
        }

        return currentAnnouncement;
    }

    public enum Level {
        LOW(new Color(0, 174, 205)),
        MEDIUM(new Color(255, 217, 21)),
        HIGH(new Color(255, 52, 52)),
        CRITICAL(new Color(133, 0, 0))
        ;

        @Getter
        private final Color color;

        Level(Color color) {
            this.color = color;
        }

        public String getHex() {
            return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        }

        public static Level getLevel(String level) {
            return Level.valueOf(level.toUpperCase());
        }
    }

    @Data
    @AllArgsConstructor
    public static class Announcement {
        private Level level;
        private String title;
        private String content;
        private boolean visible;
    }
}
