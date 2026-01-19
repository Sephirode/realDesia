package desia.progress;

import desia.Character.Enemy;
import desia.Character.Player;
import desia.item.Consumables;

import java.util.*;

public class GameSession {

    private final Random rng = new Random();

    private final Player playerBase;
    private final String playerName;
    private int level;
    private double exp = 0;

    // 현재 자원(체력, 마나)
    private double hp;
    private double mp;

    // 게임 진행도. 챕터, 액트
    private int chapter = 1;
    private int act = 1; // 1~12

    private double gold = 200; // 소지금

    // 인벤토리: 아이템 이름 -> 개수
    private final Map<String, Integer> inv = new LinkedHashMap<>();

    // 데이터(정의): 이름으로 조회
    private final Map<String, Enemy> enemyByName;
    private final Map<String, Consumables> consumableByName;

    private final ChapterRepository chapterRepo;

    // 게임 중 실제로 사용할 플레이어 객체 생성자. 오리지널 데이터 보호.
    private GameSession(Player playerBase,
                        String playerName,
                        Map<String, Enemy> enemyByName,
                        Map<String, Consumables> consumableByName,
                        ChapterRepository chapterRepo) {
        this.playerBase = playerBase;
        this.playerName = playerName;

        //
        this.level = Math.max(1, playerBase.getLevel());

        this.hp = playerBase.getMaxHp();
        this.mp = playerBase.getMaxMp();
        this.enemyByName = enemyByName;
        this.consumableByName = consumableByName;
        this.chapterRepo = chapterRepo;
    }

    public static GameSession newSession(Player chosen,
                                         List<Enemy> enemies,
                                         List<Consumables> consumables,
                                         ChapterRepository chapterRepo,
                                         String playerName) {
        Map<String, Enemy> eMap = new HashMap<>();
        for (Enemy e : enemies) eMap.put(e.getName(), e);

        Map<String, Consumables> cMap = new HashMap<>();
        for (Consumables c : consumables) cMap.put(c.getName(), c);

        GameSession s = new GameSession(chosen, playerName, eMap, cMap, chapterRepo);

        // 시작 아이템(테스트용)
        s.addItem("'보호막' 스크롤", 2);
        s.addItem("체력 포션", 3);
        return s;
    }

    public Random rng() { return rng; }

    public Player getPlayerBase() { return playerBase; }
    public String getPlayerName() { return playerName;}

    // 플레이어의 실 능력치(레벨, 성장 반영)
    public int getLevel(){ return level; }

    public void setLevel(int level){
        this.level = Math.max(1,level);
        setHp(hp);
        setMp(mp);
    }

    public double getExp(){ return exp; }

    // 레벨업 요구 경험치 함수
    public int expToNextLevel(){
        return 100 + (level - 1) * 50;
    }

    // 레벨업, 경험치 획득 메소드
    public void gainExp(double amount){
        if(amount<=0) return;
        exp += amount;

        while(exp >= expToNextLevel()){
            exp -= expToNextLevel();
            level +=1;
            // 레벨업 시 보상으로 회복
            hp = getMaxHp();
            mp = getMaxMp();

        }
    }

    // 성장한 만큼의 스탯을 계산해서 반환하는 함수
    private double scale(double base, double growth) { return base + growth*(level - 1);}

    // scale 함수를 이용한 getter 함수들
    public double getMaxHp() { return scale(playerBase.getMaxHp(), playerBase.getGrowthMaxHp());}
    public double getMaxMp() { return scale(playerBase.getMaxMp(), playerBase.getGrowthMaxMp());}
    public double getAtk() { return scale(playerBase.getAtk(), playerBase.getGrowthAtk()); }
    public double getMagic() { return scale(playerBase.getMagic(), playerBase.getGrowthMagic()); }
    public double getDef() { return scale(playerBase.getDef(), playerBase.getGrowthDef()); }
    public double getMdef() { return scale(playerBase.getMdef(), playerBase.getGrowthMdef()); }
    public double getSpd() { return scale(playerBase.getSpd(), playerBase.getGrowthSpd()); }

    // 플레이어의 현재 체력, 마나를 리턴하는 getter 함수
    public double getHp() { return hp; }
    public double getMp() { return mp; }

    /* 체력, 마나 범위를 안전하게 고정시키는 함수.
     * 값이 최대를 넘어가는 경우, 최댓값으로 잘라냄.
     * 그 후 값이 0을 넘어가는 경우, 현재 값으로 잘라냄.
     * 결과적으로 [0<=현재 값<=최댓값] 완성.
     */
    public void setHp(double hp) { this.hp = Math.max(0, Math.min(playerBase.getMaxHp(), hp)); }
    public void setMp(double mp) { this.mp = Math.max(0, Math.min(playerBase.getMaxMp(), mp)); }

    public int getChapter() { return chapter; }
    public int getAct() { return act; }
    public void setChapter(int chapter) { this.chapter = chapter; }
    public void setAct(int act) { this.act = act; }

    public double getGold() { return gold; }
    public void addGold(double delta) { this.gold = Math.max(0, this.gold + delta); }

    public Map<String, Integer> inventoryView() { return Collections.unmodifiableMap(inv); }
    public void addItem(String name, int count) {
        if (count <= 0) return;
        inv.put(name, inv.getOrDefault(name, 0) + count);
    }
    public boolean removeItem(String name, int count) {
        int cur = inv.getOrDefault(name, 0);
        if (count <= 0 || cur < count) return false;
        int next = cur - count;
        if (next == 0) inv.remove(name);
        else inv.put(name, next);
        return true;
    }

    public Enemy enemyDef(String name) {
        Enemy e = enemyByName.get(name);
        if (e == null) throw new IllegalArgumentException("unknown enemy: " + name);
        return e;
    }

    public Consumables consumableDef(String name) {
        return consumableByName.get(name);
    }

    public Collection<Consumables> allConsumables() {
        return consumableByName.values();
    }

    public ChapterConfig chapterConfig() {
        return chapterRepo.get(chapter);
    }

    public boolean isFinalChapterCleared() {
        return chapter > chapterRepo.maxChapterId();
    }
}
