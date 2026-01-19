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

    public void clearConsole() {
        for(int i=0; i<50; i++) {
            System.out.println();
        }
    }

    // 길이가 n인 문단 구분자 만들기
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

    public void Start(){
        System.out.println("\t\t\t\t\tD e s i a\n\t\t\t\t  신 화    시 대 ");
        while(true) {
            System.out.println("1. 새 게임\t2. 불러오기\t3. 랭킹\t4. 게임 종료");
            int input = io.readInt(">>>", 4);

            switch (input) {
                case 1:
                    newGame();
                    break;
                case 2:
                    gl.gameLoad();
                    break;
                case 3:
                    rk.printRanking();
                    break;
                case 4:
                    if(io.confirmExit())   //true or false. boolean 값을 리턴함.
                        return;
                    break;
                default:
                    System.out.println("잘못된 입력");
                    break;
            }
        }
    }

    public void newGame() {
        final List<Player> playables;
        try {
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
        // 세션 생성
        GameSession session;
        try {
            session = GameSession.newSession(chosen, loader.loadEnemies(), loader.loadConsumables(), chapterRepo, nickname);
        } catch (Exception e) {
            System.out.println("게임 데이터 로딩 실패: " + e.getMessage());
            return;
        }

        // 시작 스토리(개발자가 story.json에서 수정)
        storyService.printStory("game.start");
        io.anythingToContinue();

        // 캠페인 실행(챕터/액트/상점/스토리/전투)
        new CampaignEngine(io, storyService).run(session);
    }

    private String askPlayerNickname(){
        System.out.print("플레이어 이름을 정하세요: ");
        return io.readNonEmptyString(">>>",20);
    }

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

    /*public void newGame(){
        Io io1 = new Io();
        try {
            DataLoader loader = new DataLoader();
            List<Player> playables = loader.loadPlayables();
            System.out.println("플레이할 캐릭터를 선택하세요.");
            new DataLoader().printAllPlayables(playables);
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("(뒤로가기는 5 입력)");

        int input = io1.readInt(">>>",5);

        switch(input){
            case 1:
                break;
            case 2:
                break;
        }


    }*/


}
