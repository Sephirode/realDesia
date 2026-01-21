package desia.progress;

import desia.Character.Enemy;
import desia.Character.EnemyInstance;
import desia.Character.Player;
import desia.item.Consumables;

import java.util.*;

public class GameSession {

    // 랜덤함수 사용을 위해 생성
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

    // 게임 중 실제로 사용할 플레이어 객체 생성자. 오리지널 데이터를 만질 필요를 줄임. 게임 중 변동하는 값(스탯 등)을 세션 필드로 분리.
    // !!!!!주의!!!!! 이 생성자의 매개변수인 playerBase는 변수타입이 Player(객체)인 객체 변수이다.
    private GameSession(Player playerBase,
                        String playerName,
                        Map<String, Enemy> enemyByName,
                        Map<String, Consumables> consumableByName,
                        ChapterRepository chapterRepo) {
        this.playerBase = playerBase;
        this.playerName = playerName;
        /* 게임세션에서 쓸 필드를 가져온다. 체력과 마나? 그건 playerBase가 받은 플레이어 객체의 최대 체력과 최대 마나를
         * 위에서 정의한 필드들(현재 체력과 현재 마나. 줄 14~)로 가져오는 것이다. 매개변수에 없더라도 말이 되는 것.
         * 이로써 원본 정의 데이터(playerBase. Player타입 변수)와 게임 중에 실제로 사용하면서 변하는 데이터(GameSession 객체)가 구분되는 것이다.
         * 이게 없으면 툭하면 Player객체를 직접 수정하야 하는데, 이러면 게임 세이브, 로드, 전투, 리셋에서 지옥도가 열린다. */

        // 레벨 데이터가 잘못돼서 0 이하로 들어와버릴 경우, 강제로 1로 보정해준다. json의 데이터 또는 기본값이 0으로 잘못 들어와도 OK.
        this.level = Math.max(1, playerBase.getLevel());
        // 세션 시작 시, 풀피 풀마나
        this.hp = playerBase.getMaxHp();
        this.mp = playerBase.getMaxMp();
        this.enemyByName = enemyByName;
        this.consumableByName = consumableByName;
        this.chapterRepo = chapterRepo;
    }

    // ★★★★★ 접근하면 안 되는 불변 데이터(원본) 대신, 게임에서 사용할 가변 데이터 세트를(새로운 세션) 준비하는 생성자.
    // Game 클래스에서 이 생성자를 호출, 로드한 데이터를 여기에 집어넣는다.
    public static GameSession newSession(Player chosen,
                                         List<Enemy> enemies,
                                         List<Consumables> consumables,
                                         ChapterRepository chapterRepo,
                                         String playerName) {
        // 적 목록 해시맵 생성. 그 해시맵에 매개변수로 받아온(로드된 데이터. Game클래스-DataLoader클래스 순) 데이터를 배당.
        Map<String, Enemy> eMap = new HashMap<>();
        for (Enemy e : enemies) eMap.put(e.getName(), e);
        // 위와 마찬가지. 소모품 해시맵을 만들고 로드된 데이터를 매개변수로 받아와서 배당.
        Map<String, Consumables> cMap = new HashMap<>();
        for (Consumables c : consumables) cMap.put(c.getName(), c);
        // 이제 준비된 데이터들을 바탕으로 게임 중 실제로 사용할 데이터 세트(세션) 생성.
        GameSession s = new GameSession(chosen, playerName, eMap, cMap, chapterRepo);

        // 시작 아이템(테스트용)
        s.addItem("'보호막' 스크롤", 2);
        s.addItem("체력 포션", 3);
        return s;
    }

    // 정의 데이터(enemyDef)와 가변 데이터(spawnEnemy)를 나누는 수단. 이 메소드의 역할이다.
    public EnemyInstance spawnEnemy(String name) {
        return EnemyInstance.spawn(enemyDef(name), rng);
    }

    // 인레이 힌트에서 '0개의 사용 위치'라고 뜬다고 해서 정말로 안 쓰이는 게 아니다.
    // 물론, private임에도 0개면 필요없는 게 맞다.
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
    public void setHp(double hp) { this.hp = Math.max(0, Math.min(getMaxHp(), hp)); }
    public void setMp(double mp) { this.mp = Math.max(0, Math.min(getMaxMp(), mp)); }

    // 챕터, 액트의 getter, setter함수
    public int getChapter() { return chapter; }
    public int getAct() { return act; }
    public void setChapter(int chapter) { this.chapter = chapter; }
    public void setAct(int act) { this.act = act; }

    // 소지금 액수를 불러오는 getter함수 & 골드 수입을 적용하는 함수
    public double getGold() { return gold; }
    // 소지금에 delta 만큼 더한다.
    public void addGold(double delta) {
        this.gold = Math.max(0, this.gold + delta);
    }

    //
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

    // 적 '개체(객체 아님)'의 이름을 받으면, 그 적에 해당하는 적 객체를 리턴하는 함수
    public Enemy enemyDef(String name) {
        Enemy e = enemyByName.get(name);
        if (e == null)
            throw new IllegalArgumentException("unknown enemy: " + name);
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
