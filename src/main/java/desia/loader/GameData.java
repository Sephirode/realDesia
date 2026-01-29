package desia.loader;

import desia.Character.Enemy;
import desia.Character.Player;
import desia.item.Consumables;
import desia.skill.SkillDef;

import java.util.List;
import java.util.Map;

public final class GameData {
    private final List<Player> playables;
    private final Map<String, Enemy> enemies;
    private final Map<String, Consumables> consumables;
    private final Map<String, SkillDef> skills;

    public GameData(
            List<Player> playables,
            Map<String, Enemy> enemies,
            Map<String, Consumables> consumables,
            Map<String, SkillDef> skills
    ) {
        this.playables = List.copyOf(playables);
        this.enemies = Map.copyOf(enemies);
        this.consumables = Map.copyOf(consumables);
        this.skills = Map.copyOf(skills);
    }

    public List<Player> playables() { return playables; }
    public Map<String, Enemy> enemies() { return enemies; }
    public Map<String, Consumables> consumables() { return consumables; }
    public Map<String, SkillDef> skills() { return skills; }
}
