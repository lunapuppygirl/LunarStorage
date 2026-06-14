package dev.lunapuppygirl.lunarstorage.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.awt.*;

@Service
public class AnnouncementService {
    @Getter @Setter
    private Announcement currentAnnouncement;

    public void setCurrentAnnouncement(Level level, String title, String content, boolean visible) {
        this.currentAnnouncement = new  Announcement(level, title, content, visible);
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
