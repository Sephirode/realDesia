package desia.battle;

import desia.Character.EnemyInstance;
import desia.io.Io;
import desia.progress.GameSession;
import desia.ui.ConsoleUi;

import java.util.Random;

public class BattleEngine {
    private final Io io;
    private final Random random = new Random();

    public BattleEngine(Io io) {
        this.io = io;
    }

    public BattleOutcome fight(GameSession session, EnemyInstance enemy) {

        System.out.println("\n[전투] " + enemy.getName());
        System.out.println(enemy.getDescription());

        while (session.getHp() > 0 && enemy.getHp() > 0) {

            boolean playerFirst = (session.getSpd() >= enemy.getSpd());

            ConsoleUi.clearConsole();
            System.out.println("\n/Lv. " + session.getLevel() + " " + session.getPlayerName()
                    + "\nHP: " + Math.round(session.getHp()) + "/" + Math.round(session.getMaxHp())
                    + " MP: " + Math.round(session.getMp()) + "/" + Math.round(session.getMaxMp()));

            ConsoleUi.printSeparator(30);
            System.out.println("Lv. " + enemy.getLevel() + " " + enemy.getName()
                    + "\nHP: " + Math.round(enemy.getHp()) + "/" + Math.round(enemy.getMaxHp()));

            if (playerFirst) {
                TurnResult r = playerTurn(session, enemy);
                if (r == TurnResult.NO_TURN) { io.anythingToContinue(); continue; }
                if (r == TurnResult.ESCAPE) return BattleOutcome.ESCAPE;
                if (enemy.getHp() <= 0) break;

                enemyTurn(session, enemy);
            } else {
                enemyTurn(session, enemy);
                if (session.getHp() <= 0) break;

                TurnResult r = playerTurn(session, enemy);
                if (r == TurnResult.NO_TURN) { io.anythingToContinue(); continue; }
                if (r == TurnResult.ESCAPE) return BattleOutcome.ESCAPE;
            }

            io.anythingToContinue();
        }

        if (session.getHp() <= 0) {
            System.out.println("\n패배... 게임 오버");
            io.anythingToContinue();
            return BattleOutcome.LOSE;
        }

        System.out.println("\n승리!");
        io.anythingToContinue();
        return BattleOutcome.WIN;
    }


    // 이하 3개는 헬퍼 메소드
    // 플레이어 턴
    private enum TurnResult { NO_TURN, TURN_SPENT, ESCAPE }

    private TurnResult playerTurn(GameSession session, EnemyInstance enemy) {

        System.out.println("\n1. 기본 공격  2. 스킬  3. 아이템(미구현)  4. 도망");
        int cmd = io.readInt(">>>", 4);

        switch (cmd) {
            case 1 -> {
                double dmg = Math.max(1, session.getAtk() - enemy.getDef() * 0.5);
                enemy.damage(dmg);
                System.out.println("플레이어의 공격! " + Math.round(dmg) + " 피해");
                return TurnResult.TURN_SPENT;
            }
            case 2 -> {
                final double cost = 5;
                if (session.getMp() < cost) {
                    System.out.println("\nMP가 부족하다!");
                    return TurnResult.NO_TURN;
                }
                session.setMp(session.getMp() - cost);

                double dmg = Math.max(1, session.getMagic() - enemy.getMdef() * 0.5);
                enemy.damage(dmg);

                System.out.println("\n(skillname)! " + Math.round(dmg) + " 피해");
                return TurnResult.TURN_SPENT;
            }
            case 3 -> {
                System.out.println("아이템 기능 미구현.");
                return TurnResult.NO_TURN;
            }
            case 4 -> {
                // 도망 시도는 "턴 소모"
                boolean ok = tryEscape(session, enemy);
                if (ok) {
                    System.out.println("\n도망 성공!");
                    return TurnResult.ESCAPE;
                } else {
                    System.out.println("\n도망 실패!");
                    return TurnResult.TURN_SPENT;
                }
            }
            default -> {
                return TurnResult.NO_TURN;
            }
        }
    }


    // 도주 확률 공식 메소드
    private boolean tryEscape(GameSession session, EnemyInstance enemy) {

        // 속도 차이 1당 2%
        double escapeChance = (session.getSpd() - enemy.getSpd()) * 2;
        if (escapeChance <= 0)
            escapeChance = 0;

        int roll = random.nextInt(100) + 1; // 1~100

        return roll <= escapeChance;
    }


    // 적 턴
    private void enemyTurn(GameSession session, EnemyInstance enemy) {

        // 적도 기본/스킬만 있다고 치자(임시)
        // 적 스킬 사용 여부는 "적 MP"로만 내부 판단(표시는 절대 하지 않음)
        boolean canSkill = enemy.getMp() >= 5;
        boolean useSkill = canSkill && random.nextInt(100) < 40; // 40%

        double dmg;

        if (useSkill) {
            enemy.setMp(enemy.getMp() - 5);
            dmg = Math.max(1, enemy.getMagic() - session.getMdef() * 0.5);
            System.out.println("\n" + enemy.getName() + "의 스킬 공격!");
        } else {
            dmg = Math.max(1, enemy.getAtk() - session.getDef() * 0.5);
            System.out.println("\n" + enemy.getName() + "의 공격!");
        }

        session.setHp(session.getHp() - dmg);
        System.out.println(Math.round(dmg) + " 피해를 입었다.");
    }
}
