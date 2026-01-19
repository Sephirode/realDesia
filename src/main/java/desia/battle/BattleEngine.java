package desia.battle;

import desia.Character.Enemy;
import desia.Character.Player;
import desia.io.Io;
import desia.Game;
import desia.progress.GameSession;
import java.util.Random;

/*
 * 목표: "게임 루프"가 정상 작동하도록 최소 전투 엔진만 제공.
 * 밸런스/스킬/상태이상 등은 나중에 확장.
 */
public class BattleEngine {

    Game gm = new Game();
    private final Io io;
    Random random = new Random();

    public BattleEngine(Io io) {
        this.io = io;
    }

    //전투 시작.
    public boolean fight(GameSession session, Enemy enemyDef) {
        Player p = session.getPlayerBase();

        // 플레이어 현재 체력, 현재 마나
        double pHp = session.getHp();
        double pMp = session.getMp();
        // 적 레벨 (enemy Level = eLv)
        int eLv = (random.nextInt(10)+1);

        // 성장 적용: 레벨 1이면 base 스탯 그대로, 레벨이 오를수록 growth * (레벨-1)
        double eMaxHp = enemyDef.getMaxHp() + enemyDef.getGrowthMaxHp() * (eLv - 1);
        double eAtk = enemyDef.getAtk() + enemyDef.getGrowthAtk() * (eLv - 1);
        double eDef = enemyDef.getDef() + enemyDef.getGrowthDef() * (eLv - 1);
        // 적 체력 = 배당된 레벨에서의 최대 체력
        double eHp = eMaxHp;

        System.out.println("\n[전투] " + enemyDef.getName());
        System.out.println(enemyDef.getDescription());

        while (pHp > 0 && eHp > 0) {
            gm.clearConsole();
            System.out.println("\n/Lv. X "+session.getPlayerName()+"\nHP: " + Math.round(pHp) + "/" + Math.round(p.getMaxHp()) + " MP: " + Math.round(pMp) + "/" + Math.round(p.getMaxMp()));
            System.out.println("Lv. " + eLv + " " + enemyDef.getName()+"\nHP: " + Math.round(eHp) + "/" + Math.round(eMaxHp));
            System.out.println("\n1. 공격  2. 아이템(미구현)  3. 도망(미구현)");
            int cmd = io.readInt(">>>", 3);

            // 플레이어 턴: 지금은 기본공격만
            if (cmd == 1) {
                double dmg = Math.max(1, p.getAtk() - enemyDef.getDef() * 0.5);
                eHp -= dmg;
                System.out.println("플레이어의 공격! " + Math.round(dmg) + " 피해");
            } else {
                System.out.println("아직 미구현. 이번 턴은 공격으로 대체됨.");
                double dmg = Math.max(1, p.getAtk() - enemyDef.getDef() * 0.5);
                eHp -= dmg;
            }

            if (eHp <= 0) break;

            // 적 턴
            double eDmg = Math.max(1, enemyDef.getAtk() - p.getDef() * 0.5);
            pHp -= eDmg;
            System.out.println(enemyDef.getName() + "의 공격! " + Math.round(eDmg) + " 피해");
            io.anythingToContinue();
        }

        session.setHp(pHp);

        if (pHp <= 0) {
            System.out.println("\n패배... 게임 오버");
            return false;
        }
        System.out.println("\n승리!");
        io.anythingToContinue();
        return true;
    }
}
