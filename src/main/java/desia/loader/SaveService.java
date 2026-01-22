package desia.loader;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import desia.Character.Player;
import desia.io.Io;
import desia.progress.ChapterRepository;
import desia.progress.GameSession;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 저장/불러오기 서비스.
 * - 슬롯 1~3 지원
 * - 세이브 파일은 실행 폴더(user.dir)/saves/slot{n}.json 에 저장
 */
public class SaveService {

    private static final int MAX_SLOT = 3;

    private final Io io;
    private final ObjectMapper om;

    public SaveService(Io io) {
        this.io = io;
        this.om = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ====== Public API ======


    /**
     * 불러오기: 로드 성공 시 GameSession 반환, 실패/취소 시 null
     */
    public GameSession load(DataLoader loader, ChapterRepository chapterRepo) {
        ensureDir();

        System.out.println("\n[불러오기] 슬롯을 선택하세요. (1~" + MAX_SLOT + ", " + (MAX_SLOT + 1) + "=취소)");
        int cmd = io.readInt(">>>", MAX_SLOT + 1);
        if (cmd == MAX_SLOT + 1) return null;

        int slot = cmd;
        Path path = slotPath(slot);
        if (!Files.exists(path)) {
            System.out.println("해당 슬롯에 세이브가 없습니다.");
            io.anythingToContinue();
            return null;
        }

        final SaveData data;
        try {
            data = om.readValue(Files.readString(path), SaveData.class);
        } catch (IOException e) {
            System.out.println("세이브 파일을 읽을 수 없습니다: " + e.getMessage());
            io.anythingToContinue();
            return null;
        }

        // (1) 정의 데이터 로드
        final List<Player> playables;
        try {
            playables = loader.loadPlayables();
        } catch (Exception e) {
            System.out.println("플레이어블 로딩 실패: " + e.getMessage());
            io.anythingToContinue();
            return null;
        }

        Player chosen = findPlayerByClass(playables, data.getPlayerClass());
        if (chosen == null) {
            System.out.println("세이브의 직업을 찾을 수 없습니다: " + data.getPlayerClass());
            io.anythingToContinue();
            return null;
        }

        // (2) 새 세션 생성(정의 데이터 주입)
        final GameSession session;
        try {
            session = GameSession.newSession(
                    chosen,
                    loader.loadEnemies(),
                    loader.loadConsumables(),
                    chapterRepo,
                    data.getPlayerName()
            );
        } catch (Exception e) {
            System.out.println("게임 데이터 로딩 실패: " + e.getMessage());
            io.anythingToContinue();
            return null;
        }

        // (3) 세이브 상태 반영
        session.applySaveData(data);

        System.out.println("\n불러오기 완료!");
        io.anythingToContinue();
        return session;
    }

    // ====== Internal ======

    private Player findPlayerByClass(List<Player> playables, String playerClass) {
        if (playerClass == null) return null;
        for (Player p : playables) {
            if (playerClass.equals(p.getClasses())) return p;
        }
        return null;
    }

    private Path saveDir() {
        return Paths.get(System.getProperty("user.dir"), "saves");
    }

    private Path slotPath(int slot) {
        return saveDir().resolve("slot" + slot + ".json");
    }

    private void ensureDir() {
        try {
            Files.createDirectories(saveDir());
        } catch (IOException ignored) {
        }
    }

    private ObjectMapper strictMapper() {
        return new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
    }

    // ====== Save UI helper (Io readInt 제한 때문에 별도 처리) ======

    public void saveWithMenu(GameSession session) {
        ensureDir();

        System.out.println("\n[저장] 슬롯을 선택하세요. (1~" + MAX_SLOT + ", " + (MAX_SLOT + 1) + "=취소)");
        int cmd = io.readInt(">>>", MAX_SLOT + 1);
        if (cmd == MAX_SLOT + 1) return;

        int slot = cmd;
        Path path = slotPath(slot);

        if (Files.exists(path)) {
            System.out.println("이 슬롯에는 이미 세이브가 있습니다. 덮어쓸까요?");
            System.out.println("1. 예  2. 아니오");
            int yn = io.readInt(">>>", 2);
            if (yn != 1) return;
        }

        SaveData data = SaveData.builder()
                .version(1)
                .playerClass(session.getPlayerBase().getClasses())
                .playerName(session.getPlayerName())
                .chapter(session.getChapter())
                .act(session.getAct())
                .level(session.getLevel())
                .exp(session.getExp())
                .hp(session.getHp())
                .mp(session.getMp())
                .gold(session.getGold())
                .inventory(new LinkedHashMap<>(session.inventoryView()))
                .build();

        try {
            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(data);
            Files.writeString(path, json, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println("저장 완료: 슬롯 " + slot);
        } catch (IOException e) {
            System.out.println("저장 실패: " + e.getMessage());
        }

        io.anythingToContinue();
    }
}