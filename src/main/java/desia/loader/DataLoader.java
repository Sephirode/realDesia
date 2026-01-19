package desia.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import desia.Character.Enemy;
import desia.Character.Player;
import desia.item.Consumables;

import java.io.InputStream;
import java.util.List;

public class DataLoader {

    public List<Enemy> loadEnemies() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("enemies.json")) {
            if (in == null)
                throw new RuntimeException("enemies 리소스가 발견되지 않음");

            ObjectMapper om = new ObjectMapper();
            List<Enemy> enemies = om.readValue(in, new TypeReference<List<Enemy>>() {});
            return enemies;
        }
    }

    public List<Player> loadPlayables() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("playables.json")) {
            if (in == null)
                throw new RuntimeException("playables 리소스가 발견되지 않음");

            ObjectMapper om = new ObjectMapper();
            List<Player> playables = om.readValue(in, new TypeReference<List<Player>>() {});
            return playables;
        }

    }public List<Consumables> loadConsumables() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("consumables.json")) {
            if (in == null)
                throw new RuntimeException("consumables 리소스가 발견되지 않음");

            ObjectMapper om = new ObjectMapper();
            List<Consumables> consumables = om.readValue(in, new TypeReference<List<Consumables>>() {});
            return consumables;
        }
    }

    public void printAllEnemies(List<Enemy> enemies) {
        for (Enemy e : enemies)
            System.out.println(e);
    }
    public void printAllPlayables(List<Player> playables) {
        int i = 1;
        for (Player e : playables){
            System.out.println("\n"+i+") "+e.getClasses()+"\n"+e.getDescription());
            i+=1;
        }

    }
    public void printAllConsumables(List<Consumables> consumables) {
        for (Consumables e : consumables)
            System.out.println(e);
    }

}
