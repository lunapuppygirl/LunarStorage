package dev.lunapuppygirl.lunarstorage.database.services;

import dev.lunapuppygirl.lunarstorage.database.repositories.Repository;
import dev.lunapuppygirl.lunarstorage.database.repositories.blacklist.BlacklistEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BlacklistService {
    private final Logger logger = LoggerFactory.getLogger(BlacklistService.class);

    private final Repository<BlacklistEntry, UUID> blacklistRepository;

    public BlacklistService(Repository<BlacklistEntry, UUID> blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
    }

    public BlacklistEntry getByUuid(UUID uuid) {
        Optional<BlacklistEntry> entry = blacklistRepository.get(uuid);

        return entry.orElse(null);
    }

    public BlacklistEntry getByIp(String ip) {
        List<BlacklistEntry> entries = blacklistRepository.getAll(Integer.MAX_VALUE);

        try {
            InetAddress target = InetAddress.getByName(ip);

            return entries.stream()
                    .filter(e -> isInRange(target, e.getIpRange()))
                    .findFirst()
                    .orElse(null);
        } catch (UnknownHostException e) {
            logger.error("Error while getting blacklist entry: ", e);
        }

        return null;
    }

    public void deleteEntry(UUID uuid) {
        BlacklistEntry entry = getByUuid(uuid);

        if (entry == null) return;

        blacklistRepository.delete(uuid);
    }

    public void setReason(UUID uuid, String reason) {
        BlacklistEntry entry = getByUuid(uuid);

        if (entry == null) return;

        entry.setReason(reason);
        blacklistRepository.update(entry);
    }


    private boolean isInRange(InetAddress ip, String cidr) {
        try {
            String[] parts = cidr.split("/");
            InetAddress network = InetAddress.getByName(parts[0]);
            int prefixLength = Integer.parseInt(parts[1]);

            byte[] ipBytes = ip.getAddress();
            byte[] networkBytes = network.getAddress();

            if (ipBytes.length != networkBytes.length) return false;

            int fullBytes = prefixLength / 8;
            int remainingBits = prefixLength % 8;

            for (int i = 0; i < fullBytes; i++) {
                if (ipBytes[i] != networkBytes[i]) return false;
            }
            if (remainingBits > 0) {
                int mask = 0xFF & (0xFF << (8 - remainingBits));
                return (ipBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
            }
            return true;
        } catch (UnknownHostException e) {
            logger.error("Error while checking for ip in range: ", e);
        }
        return true;
    }
}
