package desia.battle;

import desia.Character.Enemy;
import desia.Character.EnemyInstance;
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

    //전투 시작. 리턴 타입이 boolean인 이유는 플레이어의 생사 여부를 true, false로 두었기 때문
    public boolean fight(GameSession session, EnemyInstance enemy) {
        Player p = session.getPlayerBase();

        // 플레이어 현재 체력, 현재 마나
        double pHp = session.getHp();
        double pMp = session.getMp();
        // 적 레벨 (enemy Level = eLv)
        int eLv = (random.nextInt(10)+1);

        /* 본래 [ 성장 적용: 레벨 1이면 base 스탯 그대로, 레벨이 오를수록 growth * (레벨-1) ]
         * -> double eDef = enemy.getDef() + enemy.getGrowthDef() * (eLv - 1); 같은 형태였으나,
         * EnemyInstance 클래스에서 scale 함수를 만들어 대체. getter 자체에 내장되어 있기 때문에
         * 여기서(BattleEngine) 복잡한 계산을 할 필요가 없다. */
        double eMaxHp = enemy.getMaxHp();
        double eAtk = enemy.getAtk();
        double eDef = enemy.getDef();
        // 적 체력 = 배당된 레벨에서의 최대 체력
        double eHp = eMaxHp;
        // 적의 이름과 정보를 출력
        System.out.println("\n[전투] " + enemy.getName());
        System.out.println(enemy.getDescription());

        // 적의 이름, 레벨, 체력 상태와 플레이어의 스탯을 출력. 그 후 행동 결정 선택지 출력
        // 플레이어의 체력 또는 적의 체력이 양수가 아닌 경우(사망한 경우) 전투 종료
        while (pHp > 0 && eHp > 0) {
            gm.clearConsole();
            //플레이어 상태 출력
            System.out.println("\n/Lv. X "+session.getPlayerName()+"\nHP: " + Math.round(pHp) + "/" + Math.round(p.getMaxHp()) + " MP: " + Math.round(pMp) + "/" + Math.round(p.getMaxMp()));
            // 구분선 및 적 상태 출력
            gm.printSeparator(30);
            System.out.println("Lv. " + eLv + " " + enemy.getName()+"\nHP: " + Math.round(eHp) + "/" + Math.round(eMaxHp));
            System.out.println("\n1. 공격  2. 아이템(미구현)  3. 도망(미구현)");
            int cmd = io.readInt(">>>", 3);

            // 플레이어 턴: 지금은 기본공격만
            if (cmd == 1) {
                double dmg = Math.max(1, p.getAtk() - enemy.getDef() * 0.5);
                eHp -= dmg;
                System.out.println("플레이어의 공격! " + Math.round(dmg) + " 피해");
            } else {
                System.out.println("아직 미구현. 이번 턴은 공격으로 대체됨.");
                double dmg = Math.max(1, p.getAtk() - enemy.getDef() * 0.5);
                eHp -= dmg;
            }
            // 적의 턴이 오기 전에 적이 사망했을 경우, 전투 종료
            if (eHp <= 0) break;

            // 적 턴
            double eDmg = Math.max(1, enemy.getAtk() - p.getDef() * 0.5);
            pHp -= eDmg;
            System.out.println(enemy.getName() + "의 공격! " + Math.round(eDmg) + " 피해");
            io.anythingToContinue();
        }
        // 전투 시작 이전에 설정된 체력을, 전투 중 변화한 체력량으로 갱신
        session.setHp(pHp);
        // 어떤 이유로든 전투 종료 시 플레이어의 체력이 0이 된 경우, 게임 오버
        if (pHp <= 0) {
            System.out.println("\n패배... 게임 오버");
            return false;
        }
        // 아니면 승리. true 값 리턴.
        System.out.println("\n승리!");
        io.anythingToContinue();
        return true;
    }
}
