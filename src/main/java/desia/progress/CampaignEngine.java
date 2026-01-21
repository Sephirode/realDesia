package desia.progress;

import desia.Game;
import desia.battle.BattleEngine;
import desia.inventory.InventoryService;
import desia.io.Io;
import desia.shop.ShopService;
import desia.story.StoryService;

import java.util.List;

/**
 * "게임 루프" 전용 엔진.
 * - 챕터(1~7), 액트(1~12)
 * - act 12는 보스전 확정
 * - act 1~11은 BATTLE/SHOP/STORY 중 하나 랜덤
 * - 스토리 문구는 story.json에서 수정 가능
 */
public class CampaignEngine {

    Game gm = new Game();
    private final Io io;
    private final StoryService story;
    private final BattleEngine battle;
    private final ShopService shop;
    private final InventoryService inv;

    public CampaignEngine(Io io, StoryService story) {
        this.io = io;
        this.story = story;
        /* battle, shop, inv는 CampaignEngine 클래스에 선언된 필드다(매개변수가 아님).
         * 생성자에서 new로 객체를 생성해 해당 필드에 대입함으로써,
         * CampaignEngine이 BattleEngine, ShopService, InventoryService 객체를 소유하게 된다.
         * 그 결과 CampaignEngine 내부에서 이 객체들의 메서드를 호출할 수 있다. */
        this.battle = new BattleEngine(io);
        this.shop = new ShopService(io);
        this.inv = new InventoryService(io);
    }

    // 게임의 메인 메뉴. 메인 루프이다. session 변수가 현재 상태에 대한 값(챕터 값, )들을 전달해준다.
    public void run(GameSession session) {
        while (!session.isFinalChapterCleared()) {
            ChapterConfig cfg = session.chapterConfig();
            printChapterActHeader(session, cfg);

            // 허브 메뉴(전투 밖)
            System.out.println("1. 진행한다  2. 스테이터스  3. 인벤토리  4. 메인메뉴로");
            int cmd = io.readInt(">>>", 4);
            if (cmd == 2) {
                printStatus(session);
                continue;
            }
            if (cmd == 3) {
                inv.open(session);
                continue;
            }
            if (cmd == 4) {
                return;
            }

            // 진행(= act 수행)
            boolean ok = resolveAct(session, cfg);
            if (!ok) return; // 게임 오버
            // 다음 act/chapter
            advance(session);
        }

        story.printStory("game.clear");
    }

    private void printChapterActHeader(GameSession session, ChapterConfig cfg) {
        var p = session.getPlayerBase();
        gm.clearConsole();
        gm.printSeparatorX(30);
        System.out.println("CHAPTER " + cfg.getId() + " - " + cfg.getName());
        System.out.println("ACT " + session.getAct() + "/12");
        gm.printSeparatorX(30);

        System.out.println(session.getPlayerName()+"\tLv. "+p.getLevel());
        System.out.println("HP: " + Math.round(session.getHp()) + "/" + Math.round(p.getMaxHp()));
        System.out.println("MP: " + Math.round(session.getMp()) + "/" + Math.round(p.getMaxMp()));
    }

    private void printStatus(GameSession session) {
        var p = session.getPlayerBase();
        gm.clearConsole();
        gm.printHeading("[스테이터스]",1);
        System.out.println("이름: " + session.getPlayerName());
        System.out.println("레벨: " + p.getLevel());
        System.out.println("HP: " + Math.round(session.getHp()) + "/" + Math.round(p.getMaxHp()));
        System.out.println("MP: " + Math.round(session.getMp()) + "/" + Math.round(p.getMaxMp()));
        System.out.println("ATK: " + p.getAtk() + "  DEF: " + p.getDef());
        System.out.println("소지금: " + Math.round(session.getGold())+"골드");
        io.anythingToContinue();
    }

    // 현재 챕터와 챕터의 몬스터 풀에 대한 정보를 수신함
    private boolean resolveAct(GameSession session, ChapterConfig cfg) {
        int act = session.getAct();
        // 마지막(12번째) 액트 상태일 경우, 최종보스와 전투 시작
        if (act == 12) {
            story.printStory("chapter." + cfg.getId() + ".boss");
            return doBattle(session, cfg.getBoss());
        }

        //
        ActType type = rollActType(session, cfg);
        switch (type) {
            case SHOP:
                story.printStory("chapter." + cfg.getId() + ".shop");
                shop.open(session);
                return true;
            case STORY:
                String key = rollStoryKey(session, cfg);
                if (key != null) story.printStory(key);
                else story.printStory("story.fallback");
                return true;
            case BATTLE:
            default:
                story.printStory("chapter." + cfg.getId() + ".battle");
                String enemyName = rollEnemy(session, cfg);
                return doBattle(session, enemyName);
        }
    }

    // 전투 함수 fight에 정보를 넘겨주는 메소드
    private boolean doBattle(GameSession session, String enemyName) {
        try {
            var enemy = session.spawnEnemy(enemyName);
            boolean win = battle.fight(session, enemy);
            if (!win) return false;

            // 보상(임시 50골드)
            // TODO: 보상 함수 제작할 것.
            session.addGold(50);
            return true;
        } catch (Exception e) {
            System.out.println("전투 시작 실패: " + e.getMessage());
            return true; // 루프를 막지 않게 일단 진행
        }
    }

    // 액트에서 어떤 이벤트를 불러올지 랜덤으로 정하는 메소드. 보통 전투가 나온다.
    private ActType rollActType(GameSession session, ChapterConfig cfg) {
        // 간단 가중치: 70% 전투, 10% 상점, 20% 스토리
        int r = session.rng().nextInt(100);
        if (r < 70) return ActType.BATTLE;
        if (r < 80) return ActType.SHOP;
        // ChapterConfig에서 이미 선언해둔 리스트. 이 리스트(ChapterConfig타입)는 int id, String name 필드를 가짐.
        List<String> keys = cfg.getStoryKeys();
        // 스토리 키가 없으면 전투
        if (keys == null || keys.isEmpty()) return ActType.BATTLE;
        return ActType.STORY;
    }

    private String rollEnemy(GameSession session, ChapterConfig cfg) {
        List<String> pool = cfg.getEnemyPool();
        if (pool == null || pool.isEmpty()) {
            throw new IllegalStateException("enemyPool 비어있음. chapters.json 확인 필요");
        }
        return pool.get(session.rng().nextInt(pool.size()));
    }

    private String rollStoryKey(GameSession session, ChapterConfig cfg) {
        List<String> keys = cfg.getStoryKeys();
        if (keys == null || keys.isEmpty()) return null;
        return keys.get(session.rng().nextInt(keys.size()));
    }

    private void advance(GameSession session) {
        int act = session.getAct();
        if (act < 12) {
            session.setAct(act + 1);
            return;
        }

        // 챕터 클리어
        story.printStory("chapter.clear");
        session.setChapter(session.getChapter() + 1);
        session.setAct(1);
    }
}
