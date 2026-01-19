package desia.inventory;

import desia.io.Io;
import desia.item.Consumables;
import desia.progress.GameSession;
import desia.Game;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 최소 인벤토리: 소모품 사용(전투 외) + 판매.
 */
public class InventoryService {

    Game gm = new Game();
    private final Io io;

    public InventoryService(Io io) {
        this.io = io;
    }

    public void open(GameSession session) {
        while (true) {
            gm.clearConsole();
            gm.printHeading("[인벤토리]",1);
            Map<String, Integer> inv = session.inventoryView();
            List<String> names = new ArrayList<>(inv.keySet());
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                System.out.println((i + 1) + ") " + name + " x" + inv.get(name));
            }
            System.out.println("\n[인벤토리] 소지금: " + Math.round(session.getGold())+"골드");
            System.out.println("1. 아이템 사용  2. 나가기");
            int cmd = io.readInt(">>>", 3);

            if (cmd == 1) {
                useMenu(session);
            } else {
                return;
            }
        }
    }

    private void useMenu(GameSession session) {
        Map<String, Integer> inv = session.inventoryView();
        if (inv.isEmpty()) {
            System.out.println("인벤토리가 비었습니다.");
            return;
        }

        List<String> names = new ArrayList<>(inv.keySet());
        while (true) {
            System.out.println("\n[사용] (0 입력: 뒤로)");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                System.out.println((i + 1) + ") " + name + " x" + inv.get(name));
            }
            int input = readIntAllowZero(names.size());
            if (input == 0) return;

            String name = names.get(input - 1);
            Consumables c = session.consumableDef(name);
            if (c == null) {
                System.out.println("정의되지 않은 아이템이라 사용 불가: " + name);
                io.anythingToContinue();
                continue;
            }
            if (!c.isUseOutOfBattle()) {
                System.out.println("전투 밖에서는 사용할 수 없습니다.");
                io.anythingToContinue();
                continue;
            }
            if (!session.removeItem(name, 1)) {
                System.out.println("사용 실패");
                io.anythingToContinue();
                continue;
            }
            applyConsumable(session, c);
            System.out.println("사용 완료: " + name);
            io.anythingToContinue();
        }
    }

    private void applyConsumable(GameSession session, Consumables c) {
        // 지금은 최소 구현: hp/mp 회복만 반영
        if (c.getHp() != 0) session.setHp(session.getHp() + c.getHp());
        if (c.getMp() != 0) session.setMp(session.getMp() + c.getMp());
    }

    private void sellMenu(GameSession session) {
        Map<String, Integer> inv = session.inventoryView();
        if (inv.isEmpty()) {
            System.out.println("인벤토리가 비었습니다.");
            io.anythingToContinue();
            return;
        }

        List<String> names = new ArrayList<>(inv.keySet());
        while (true) {
            System.out.println("\n[판매] (0 입력: 뒤로)");
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                double unit = sellPrice(session, name);
                System.out.println((i + 1) + ") " + name + " x" + inv.get(name) + " - 개당 " + Math.round(unit) + "G");
            }
            int input = readIntAllowZero(names.size());
            if (input == 0) return;

            String name = names.get(input - 1);
            if (!session.removeItem(name, 1)) {
                System.out.println("판매 실패");
                io.anythingToContinue();
                continue;
            }
            double gain = sellPrice(session, name);
            session.addGold(gain);
            System.out.println("판매 완료: " + name + " (" + Math.round(gain) + "G)");
            io.anythingToContinue();
        }
    }

    private double sellPrice(GameSession session, String itemName) {
        Consumables def = session.consumableDef(itemName);
        double base = (def == null) ? 10 : def.getPrice();
        return Math.max(1, Math.floor(base * 0.5));
    }

    private int readIntAllowZero(int max) {
        while (true) {
            try {
                int v = Integer.parseInt(new java.util.Scanner(System.in).next());
                if (v >= 0 && v <= max) return v;
            } catch (Exception ignored) {}
            System.out.println("잘못된 입력");
            io.anythingToContinue();
        }
    }
}
