package desia.combat;

public interface ShieldCarrier {
    double getShield();
    void setShield(double shield);

    default void addShield(double amount) {
        if (amount <= 0) return;
        setShield(Math.max(0, getShield() + amount));
    }

    // @return 실드로 흡수 후 HP로 들어갈 남은 피해
    default double absorbDamage(double incomingDamage) {
        if (incomingDamage <= 0) return 0;
        double s = Math.max(0, getShield());
        if (s <= 0) return incomingDamage;

        if (incomingDamage >= s) {
            setShield(0);
            return incomingDamage - s;
        } else {
            setShield(s - incomingDamage);
            return 0;
        }
    }
}
