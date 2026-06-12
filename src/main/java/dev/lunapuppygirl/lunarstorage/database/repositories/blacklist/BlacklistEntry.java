package dev.lunapuppygirl.lunarstorage.database.repositories.blacklist;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class BlacklistEntry {
    private UUID uuid;
    private String ipRange;
    private String reason;
}
