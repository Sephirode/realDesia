package desia;

import desia.Character.Player;
import desia.io.Io;
import desia.loader.DataLoader;
import desia.loader.GameLoad;
import desia.loader.Ranking;
import desia.progress.GameSession;
import desia.progress.CampaignEngine;
import desia.progress.ChapterRepository;
import desia.story.StoryRepository;
import desia.story.StoryService;

import java.util.List;

public class Game {

    // 공용 객체: 한 번만 만들어서 계속 사용
    private final Io io = new Io();
    private final DataLoader loader = new DataLoader();
    private final GameLoad gl = new GameLoad();
    private final Ranking rk = new Ranking();

    // 진행/스토리
    private final ChapterRepository chapterRepo = new ChapterRepository();
    private final StoryService storyService = new StoryService(new StoryRepository());

    // 게임을 종료하기 전까지, 게임 진행상황은 Game 클래스가 들고 있는다.
    private GameSession currentSession = null;

    // 게임 시작. 메인함수의 첫 시작점.
    public void start(){
        System.out.println("\t\t\t\t\tD e s i a\n\t\t\t\t  신 화    시 대 ");
        while(true) {
            System.out.println("1. 새 게임\t2. 계속하기\t3. 불러오기\t4. 랭킹\t5. 게임 종료");
            int input = io.readInt(">>>", 5);
            // arrow switch문은 내부적으로 break가 강제되어 있는 형태이다. break문이 필요 없다.
            switch (input) {
                case 1 -> newGame();
                case 2 -> continueGame();
                case 3 -> gl.gameLoad();
                case 4 -> rk.printRanking();
                case 5 -> { if (io.confirmExit()) return; }
            }
        }
    }

    // 새 게임 시작하는 메소드.
    // ★★★★★로드된 데이터 -> 세션으로의 이동 코드는 여기에 있다.★★★★★
    public void newGame() {
        // 게임이 진행 중인 상태에서 새로운 게임을 시작할 경우, 플레이어에게 체크 요구
        if (currentSession != null) {
            System.out.println("진행 중인 게임이 있습니다. 새 게임을 시작하시겠습니까?");
            System.out.println("1. 예  2. 아니오");
            int input = io.readInt(">>>", 2);
            if (input == 2) return;
        }

        final List<Player> playables;
        try {
            // DataLoad 클래스의 loadPlayables함수가 리턴한 playables 객체 리스트를, 이곳에 있는 객체 리스트에 배당해줌.
            playables = loader.loadPlayables();
        } catch (Exception e) {
            System.out.println("플레이어블 로딩 실패: " + e.getMessage());
            return;
        }

        int selectedIndex = selectPlayable(playables); // -1이면 뒤로가기
        if (selectedIndex == -1) return;

        Player chosen = playables.get(selectedIndex);

        // 닉네임 입력(직업classes와 분리)
        String nickname = askPlayerNickname();

        // ★★★★★세션 생성. 여기가 바로 DataLoader 클래스에서 로드한 json 데이터를 GameSesseion 클래스로 넘겨주는 구간이다.
        GameSession session;
        try {
            session = GameSession.newSession(chosen, loader.loadEnemies(), loader.loadConsumables(), chapterRepo, nickname);
        } catch (Exception e) {
            System.out.println("게임 데이터 로딩 실패: " + e.getMessage());
            return;
        }

        // 시작 스토리(개발자가 story.json에서 수정)
        currentSession = session;
        storyService.printStory("game.start");
        // 캠페인 실행(챕터/액트/상점/스토리/전투)
        /* 이 코드는 원래 다음 두 줄의 코드를 압축한 것이다.
         * CampaignEngine engine = new CampaignEngine(io, storyService);
         * engine.run(session);
         * 즉, 생성자 호출과 객체 생성을 한꺼번에 하고(new CampaignEngine(io, storyService)),
         * 실행(.run())까지 동시에 한 것이다. */
        new CampaignEngine(io, storyService).run(session);
    }
    //
    private void continueGame(){
        if(currentSession == null){
            System.out.println("진행 중인 게임이 없습니다.");
            return;
        }
        new CampaignEngine(io, storyService).run(currentSession);
    }

    // 플레이어블 캐릭터 선택하는 메소드
    private int selectPlayable(List<Player> playables) {
        while (true) {
            System.out.println("플레이할 캐릭터를 선택하세요.");
            loader.printAllPlayables(playables);
            System.out.println("(뒤로가기는 5 입력)");

            int input = io.readInt(">>>", 5);

            if (input == 5) return -1;

            int idx = input - 1; // 1 -> 0
            if (idx >= 0 && idx < playables.size()) return idx;

            System.out.println("잘못된 입력");
        }
    }

    // 플레이어 이름 정하는 메소드
    private String askPlayerNickname(){
        System.out.print("플레이어 이름을 정하세요: ");
        return io.readNonEmptyString(">>>",20);
    }

    // 보고 있는 콘솔 화면 정리하기
    public void clearConsole() {
        for(int i=0; i<50; i++) {
            System.out.println();
        }
    }

    // 길이가 n인 문단 구분자 만들기 - 2개 버전 (----- / =====)
    public void printSeparator(int n) {
        for(int i=0;i<n;i++) {
            System.out.print("-");
        }System.out.println();
    }
    public void printSeparatorX(int n){
        for(int i=0;i<n;i++) {
            System.out.print("=");
        }System.out.println();
    }

    // 제목 출력하기
    // 2번은 - 대신 =을 출력하는 굵은 버전.
    public void printHeading(String title, int n) {
        switch (n){
            case 1:
                printSeparator(30);
                System.out.println(title);
                printSeparator(30);
                break;
            case 2:
                printSeparatorX(30);
                System.out.println(title);
                printSeparatorX(30);
        }
    }
}
