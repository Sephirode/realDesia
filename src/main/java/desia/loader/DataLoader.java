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
        // json파일을 resources파일에서 찾은 뒤, 인풋스트림을 꽂는다
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("enemies.json")) {
            if (in == null)
                throw new RuntimeException("enemies 리소스가 발견되지 않음");
            // json-java간 변환기 소환!
            ObjectMapper om = new ObjectMapper();
            // jackson이 json을 읽으면서 읽은 것 하나당 Enemy 객체를 1개씩 생성한다. 이렇게 생성된 객체 리스트의 이름이 enemies다.
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

    // 향상된 for문을 이용해서, 생성한 리스트의 객체들을 전부 출력하는 메소드들. 향상된 for문은 배열이나 리스트를 처음부터 끝까지 전부 출력한다.
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
