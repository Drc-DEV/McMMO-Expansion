package pro.dracarys.mcmmoexpansion;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.PartyAPI;
import com.gmail.nossr50.database.FlatfileDatabaseManager;
import com.gmail.nossr50.database.SQLDatabaseManager;
import com.gmail.nossr50.datatypes.database.PlayerStat;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.datatypes.skills.PrimarySkillType;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.player.UserManager;
import me.clip.placeholderapi.PlaceholderAPIPlugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class McMMOExpansion extends PlaceholderExpansion {
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin(this.getPlugin()) != null;
    }

    public String getAuthor() {
        return "xmarinusx";
    }

    public String getIdentifier() {
        return "mcmmo";
    }

    public String getPlugin() {
        return "mcMMO";
    }

    public String getVersion() {
        return "2.0.0";
    }

    public String onPlaceholderRequest(final Player p, final String identifier) {
        if (p == null) {
            return "";
        }
        McMMOPlayer player = null;
        try {
            player = UserManager.getPlayer(p);
        } catch (Exception e) {
            return "";
        }
        if (player == null) {
            return "";
        }
        if (identifier.startsWith("level_")) {
            final String skill = identifier.split("level_")[1];
            return this.getSkillLevel(player, skill);
        }
        if (identifier.startsWith("rank_")) {
            final String skill = identifier.split("rank_")[1];
            return this.getSkillRank(p, skill);
        }
        if (identifier.startsWith("xp_remaining_")) {
            final String skill = identifier.split("xp_remaining_")[1];
            return this.getXPRemaining(p, skill);
        }
        if (identifier.startsWith("xp_needed_")) {
            final String skill = identifier.split("xp_needed_")[1];
            return this.getXPToNextLevel(p, skill);
        }
        if (identifier.startsWith("xp_")) {
            final String skill = identifier.split("xp_")[1];
            return this.getSkillXP(p, skill);
        }
        switch (identifier) {
            case "power_level": {
                return String.valueOf(player.getPowerLevel());
            }
            case "power_level_cap": {
                return String.valueOf(ExperienceAPI.getPowerLevelCap());
            }
            case "in_party": {
                return PartyAPI.inParty(p) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
            }
            case "party_name": {
                return (PartyAPI.getPartyName(p) != null) ? PartyAPI.getPartyName(p) : "";
            }
            case "party_leader": {
                return this.getPartyLeader(p);
            }
            case "is_party_leader": {
                return this.getPartyLeader(p).equals(p.getName()) ? PlaceholderAPIPlugin.booleanTrue() : PlaceholderAPIPlugin.booleanFalse();
            }
            case "party_size": {
                return (PartyAPI.getMembersMap(p) != null) ? String.valueOf(PartyAPI.getMembersMap(p).size()) : "0";
            }
            default: {
                return null;
            }
        }
    }

    private String getPartyLeader(final Player p) {
        if (PartyAPI.getPartyName(p) == null) {
            return "";
        }
        final String leader = PartyAPI.getPartyLeader(PartyAPI.getPartyName(p));
        return (leader != null) ? leader : "";
    }

    private String getSkillLevel(final McMMOPlayer p, final String skill) {
        switch (skill) {
            case "acrobatics": {
                return String.valueOf(p.getAcrobaticsManager().getSkillLevel());
            }
            case "alchemy": {
                return String.valueOf(p.getAlchemyManager().getSkillLevel());
            }
            case "archery": {
                return String.valueOf(p.getArcheryManager().getSkillLevel());
            }
            case "axes": {
                return String.valueOf(p.getAxesManager().getSkillLevel());
            }
            case "excavation": {
                return String.valueOf(p.getExcavationManager().getSkillLevel());
            }
            case "fishing": {
                return String.valueOf(p.getFishingManager().getSkillLevel());
            }
            case "herbalism": {
                return String.valueOf(p.getHerbalismManager().getSkillLevel());
            }
            case "mining": {
                return String.valueOf(p.getMiningManager().getSkillLevel());
            }
            case "repair": {
                return String.valueOf(p.getRepairManager().getSkillLevel());
            }
            case "salvage": {
                return String.valueOf(p.getSalvageManager().getSkillLevel());
            }
            case "smelting": {
                return String.valueOf(p.getSmeltingManager().getSkillLevel());
            }
            case "swords": {
                return String.valueOf(p.getSwordsManager().getSkillLevel());
            }
            case "taming": {
                return String.valueOf(p.getTamingManager().getSkillLevel());
            }
            case "unarmed": {
                return String.valueOf(p.getUnarmedManager().getSkillLevel());
            }
            case "woodcutting": {
                return String.valueOf(p.getWoodcuttingManager().getSkillLevel());
            }
            default: {
                return null;
            }
        }
    }

    private String getSkillRank(final Player p, final String skill) {
        if (!ExperienceAPI.isValidSkillType(skill.toUpperCase())) {
            return "";
        }
        if (mcMMO.getDatabaseManager() instanceof SQLDatabaseManager) {
            return String.valueOf(ExperienceAPI.getPlayerRankSkill(p.getUniqueId(), skill.toUpperCase()));
        }
        if (!(mcMMO.getDatabaseManager() instanceof FlatfileDatabaseManager)) {
            return "";
        }
        try {
            final FlatfileDatabaseManager dbManager = (FlatfileDatabaseManager) mcMMO.getDatabaseManager();
            final Method privateStringMethod = ExperienceAPI.class.getDeclaredMethod("getNonChildSkillType", String.class);
            final Method getPlayerRankMethod = FlatfileDatabaseManager.class.getDeclaredMethod("getPlayerRank", String.class, List.class);
            final Field playerStatHashField = FlatfileDatabaseManager.class.getDeclaredField("playerStatHash");
            privateStringMethod.setAccessible(true);
            playerStatHashField.setAccessible(true);
            getPlayerRankMethod.setAccessible(true);
            final PrimarySkillType skillType = (PrimarySkillType) privateStringMethod.invoke(ExperienceAPI.class, skill);
            final HashMap<PrimarySkillType, List<PlayerStat>> playerStatHash = (HashMap<PrimarySkillType, List<PlayerStat>>) playerStatHashField.get(dbManager);
            final Integer playerRank = (Integer) getPlayerRankMethod.invoke(dbManager, p.getName(), playerStatHash.get(skillType));
            return String.valueOf(playerRank);
        } catch (Exception e) {
            System.out.println("A stupid error occurred with reflections:");
            e.printStackTrace();
            return "";
        }
    }

    private String getSkillXP(final Player p, final String skill) {
        if (!ExperienceAPI.isValidSkillType(skill.toUpperCase())) {
            return "";
        }
        return String.valueOf(ExperienceAPI.getXP(p, skill.toUpperCase()));
    }

    private String getXPRemaining(final Player p, final String skill) {
        if (!ExperienceAPI.isValidSkillType(skill.toUpperCase())) {
            return "";
        }
        return String.valueOf(ExperienceAPI.getXPRemaining(p, skill.toUpperCase()));
    }

    private String getXPToNextLevel(final Player p, final String skill) {
        if (!ExperienceAPI.isValidSkillType(skill.toUpperCase())) {
            return "";
        }
        return String.valueOf(ExperienceAPI.getXPToNextLevel(p, skill.toUpperCase()));
    }
}