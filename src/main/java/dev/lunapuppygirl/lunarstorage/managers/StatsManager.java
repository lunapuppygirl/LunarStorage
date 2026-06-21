package dev.lunapuppygirl.lunarstorage.managers;

import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Calendar;

@Component
public class StatsManager {

    private final JsonFileManager jsonFileManager;

    public StatsManager(JsonFileManager jsonFileManager) {
        this.jsonFileManager = jsonFileManager;
    }

    public void logRequest() {
        Calendar calendar = Calendar.getInstance();

        int calendarDay = calendar.get(Calendar.DAY_OF_WEEK);
        int day = (calendarDay == Calendar.SUNDAY) ? 7 : calendarDay - 1; // remap to 1=monday..7=sunday
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        long millis = calendar.getTimeInMillis();
        int weekEpoch = (int) (millis / (7L * 24 * 60 * 60 * 1000));
        int dayEpoch = (int) (millis / (24L * 60 * 60 * 1000));
        int hourEpoch = (int) (millis / (60L * 60 * 1000));

        var statsFile = jsonFileManager.getStatsFile();

        resetSlotIfStale("requests.epochs.week_day_epochs", day, weekEpoch, "requests.last_week", statsFile);
        resetSlotIfStale("requests.epochs.day_hour_epochs", hour, dayEpoch, "requests.last_day", statsFile);
        resetSlotIfStale("requests.epochs.hour_minute_epochs", minute, hourEpoch, "requests.last_hour", statsFile);

        incrementSlot("requests.last_week", String.valueOf(day), statsFile);
        incrementSlot("requests.last_day", String.valueOf(hour), statsFile);
        incrementSlot("requests.last_hour", String.valueOf(minute), statsFile);

        jsonFileManager.setLong("updated", millis, statsFile);
    }

    private void resetSlotIfStale(String epochBasePath, int slot, int currentEpoch, String dataBasePath, File statsFile) {
        String epochPath = "%s.%d".formatted(epochBasePath, slot);
        int storedEpoch = jsonFileManager.getInt(epochPath, statsFile, -1);

        if (storedEpoch != currentEpoch) {
            jsonFileManager.setInt("%s.%d".formatted(dataBasePath, slot), 0, statsFile);
            jsonFileManager.setInt(epochPath, currentEpoch, statsFile);
        }
    }

    private void incrementSlot(String basePath, String slot, File statsFile) {
        String path = "%s.%s".formatted(basePath, slot);
        int oldValue = jsonFileManager.getInt(path, statsFile, 0);
        jsonFileManager.setInt(path, oldValue + 1, statsFile);
    }
}