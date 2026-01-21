package desia.Character;

import java.util.Objects;
import java.util.Random;

/* 전투 중에만 존재하는 적 개체.
 * Enemy(def)는 json에서 로딩된, 정의된 원본 데이터이므로 수정 금지.
 * EnemyInstance는 전투에서 HP, 상태이상 등이 변하는 런타임 객체. */

public class EnemyInstance {

    private final Enemy def;
    private final int level;

    private double hp;
    private double mp;

    public EnemyInstance(Enemy def, int level){
        this.def = Objects.requireNonNull(def, "def");
        this.level= Math.max(1, level);
        this.hp = getMaxHp();
        this.mp = getMaxMp();
    }

    // 기존 BattleEngine 방식 유지: 1~10 랜덤 레벨
    public static EnemyInstance spawn(Enemy def, Random rng) {
        // 삼항연산자. 랜덤함수가 제대로 작동하지 않았을 경우 1~10값 생성
        int lv = (rng == null) ? 1 : (rng.nextInt(10) + 1);
        return new EnemyInstance(def, lv);
    }

    // getter함수. Enemy 객체의 이름, 설명, 레벨을 리턴한다.
    public String getName() { return def.getName(); }
    public String getDescription() { return def.getDescription(); }
    public int getLevel() { return level; }

    private double scale(double base, double growth) {
        return base + growth * (level - 1);
    }

    // getter함수. 이쪽은 최대 체력, 최대 마나, 공격력, 방어력 리턴.
    // todo: 나머지 스탯들도 추가할 것
    public double getMaxHp() { return scale(def.getMaxHp(), def.getGrowthMaxHp()); }
    public double getMaxMp() { return scale(def.getMaxMp(), def.getGrowthMaxMp()); }
    public double getAtk()   { return scale(def.getAtk(), def.getGrowthAtk()); }
    public double getDef(){ return scale(def.getDef(), def.getGrowthDef()); }

    // 현재 체력 리턴하는 getter함수.
    public double getHp() { return hp; }
    // 체력이 최댓값을 초과하거나 음수가 되는 것을 방지하는 setter함수.
    // 1단계: min 함수로 최대 체력을 넘지 않도록 보정한다. 2단계: max 함수로 체력이 음수가 되는 것을 방지한다.
    public void setHp(double hp) { this.hp = Math.max(0, Math.min(getMaxHp(), hp)); }
    // 받은 대미지 계산하는 함수. 받은 대미지 amount가 0보다 작으면 아무 변화가 없다.
    public void damage(double amount) { if (amount > 0) setHp(hp - amount); }

}
