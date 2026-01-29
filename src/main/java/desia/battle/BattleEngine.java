package desia.battle;

import desia.Character.EnemyInstance;
import desia.io.Io;
import desia.progress.GameSession;
import desia.ui.ConsoleUi;
import desia.status.StatusEngine;
import desia.skill.*;
import desia.combat.DamageEngine;
import desia.combat.DamageType;

import java.util.Random;
import java.util.*;

public class BattleEngine {
    private final Io io;
    private final Random random = new Random();
    private final SkillSetRepository skillSets = new SkillSetRepository();
    private final SkillEngine skillEngine = new SkillEngine(random);


    public BattleEngine(Io io) {
        this.io = io;
    }

    public BattleOutcome fight(GameSession session, EnemyInstance enemy) {

        // 상태이상 초기화
        session.resetBattleStatuses();

        System.out.println("\n[전투] " + enemy.getName());
        System.out.println(enemy.getDescription());

        while (session.getHp() > 0 && enemy.getHp() > 0) {

            boolean playerFirst = (session.getSpd() >= enemy.getSpd());

            ConsoleUi.clearConsole();
            System.out.println("\n/Lv. " + session.getLevel() + " " + session.getPlayerName()
                    + "\nHP: " + Math.round(session.getHp()) + "/" + Math.round(session.getMaxHp())
                    + " MP: " + Math.round(session.getMp()) + "/" + Math.round(session.getMaxMp())
                    + " SHD: " + Math.round(session.getShield()));

            ConsoleUi.printSeparator(30);
            System.out.println("Lv. " + enemy.getLevel() + " " + enemy.getName()
                    + "\nHP: " + Math.round(enemy.getHp()) + "/" + Math.round(enemy.getMaxHp())
                    + " SHD: " + Math.round(enemy.getShield()));

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

            // === 엔드 페이즈 ===
            // 상태이상 스택 감소
            if (session.getHp() > 0 && enemy.getHp() > 0) {
                int pDot = StatusEngine.applyEndPhase(session);
                int eDot = StatusEngine.applyEndPhase(enemy);
                if (pDot > 0) System.out.println("\n[엔드] " + session.getPlayerName() + " 도트 피해: " + pDot);
                if (eDot > 0) System.out.println("\n[엔드] " + enemy.getName() + " 도트 피해: " + eDot);
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

        // 행동 불가(스택형 제어계)
        if (StatusEngine.blocksAction(session)) {
            System.out.println("\n" + StatusEngine.blockReason(session) + " 상태로 행동할 수 없다!");
            return TurnResult.TURN_SPENT;
        }

        System.out.println("\n1. 기본 공격  2. 스킬  3. 아이템(미구현)  4. 도망");
        int cmd = io.readInt(">>>", 4);

        switch (cmd) {
            case 1 -> {
                double raw = Math.max(1, session.getAtk());
                double dealtToHp = DamageEngine.deal(session, enemy, raw, DamageType.PHYSICAL, 1);
                double finalDmg = Math.max(1, raw - enemy.getDef() * 0.5);
                double absorbed = Math.max(0, finalDmg - dealtToHp);
                if (absorbed > 0) {
                    System.out.println("플레이어의 공격! 실드 " + Math.round(absorbed) + " 흡수 + HP " + Math.round(dealtToHp) + " 피해");
                } else {
                    System.out.println("플레이어의 공격! " + Math.round(dealtToHp) + " 피해");
                }
                return TurnResult.TURN_SPENT;
            }
            case 2 -> {
                String classes = session.getPlayerBase().getClasses(); // 네 프로젝트 필드명에 맞춰 필요 시 수정

                List<String> nameList = skillSets.rawSets().getOrDefault(classes, List.of());
                List<String> validNames = new ArrayList<>();
                List<SkillDef> defs = new ArrayList<>();

                for (String n : nameList) {
                    SkillDef def = session.skillDef(n);
                    if (def != null) {
                        validNames.add(n);
                        defs.add(def);
                    }
                }

                if (defs.isEmpty()) {
                    System.out.println("\n사용 가능한 스킬이 없다. (resources/skillsets.json 확인)");
                    return TurnResult.NO_TURN;
                }

                System.out.println("\n스킬을 선택하세요. (뒤로가기는 " + (defs.size() + 1) + " 입력)");
                for (int i = 0; i < defs.size(); i++) {
                    SkillDef d = defs.get(i);
                    System.out.println((i + 1) + ") " + validNames.get(i) + "  (MP: " + d.getMpCost() + ")");
                }

                int pick = io.readInt(">>>", defs.size() + 1);
                if (pick == defs.size() + 1) return TurnResult.NO_TURN;

                String chosenName = validNames.get(pick - 1);
                SkillDef chosen = defs.get(pick - 1);

                SkillCastResult r = skillEngine.cast(chosenName, chosen, session, enemy);
                for (String line : r.getLogs()) System.out.println(line);
                io.anythingToContinue();

                return r.isSpentTurn() ? TurnResult.TURN_SPENT : TurnResult.NO_TURN;
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

        // 행동 불가(스택형 제어계)
        if (StatusEngine.blocksAction(enemy)) {
            System.out.println("\n" + enemy.getName() + "은(는) " + StatusEngine.blockReason(enemy) + " 상태로 행동할 수 없다!");
            return;
        }

        // 적도 기본/스킬만 있다고 치자(임시)
        // 적 스킬 사용 여부는 "적 MP"로만 내부 판단(표시는 절대 하지 않음)
        boolean canSkill = enemy.getMp() >= 5;
        boolean useSkill = canSkill && random.nextInt(100) < 40; // 40%

        double dmg;

        if (useSkill) {
            enemy.setMp(enemy.getMp() - 5);
            dmg = Math.max(1, enemy.getMagic());
            System.out.println("\n" + enemy.getName() + "의 스킬 공격!");
            double dealtToHp = DamageEngine.deal(enemy, session, dmg, DamageType.MAGIC, 1);
            double finalDmg = Math.max(1, dmg - session.getMdef() * 0.5);
            double absorbed = Math.max(0, finalDmg - dealtToHp);
            if (absorbed > 0) System.out.println("실드 " + Math.round(absorbed) + " 흡수 + HP " + Math.round(dealtToHp) + " 피해를 입었다.");
            else System.out.println(Math.round(dealtToHp) + " 피해를 입었다.");
        } else {
            dmg = Math.max(1, enemy.getAtk());
            System.out.println("\n" + enemy.getName() + "의 공격!");
            double dealtToHp = DamageEngine.deal(enemy, session, dmg, DamageType.PHYSICAL, 1);
            double finalDmg = Math.max(1, dmg - session.getDef() * 0.5);
            double absorbed = Math.max(0, finalDmg - dealtToHp);
            if (absorbed > 0) System.out.println("실드 " + Math.round(absorbed) + " 흡수 + HP " + Math.round(dealtToHp) + " 피해를 입었다.");
            else System.out.println(Math.round(dealtToHp) + " 피해를 입었다.");
        }
    }
}
