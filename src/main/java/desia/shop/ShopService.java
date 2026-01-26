package desia.shop;

import desia.io.Io;
import desia.item.Consumables;
import desia.progress.GameSession;
import desia.ui.ConsoleUi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 최소 상점: 소모품 구매/판매.
 * - 아이템 풀은 consumables.json
 * - 밸런스는 나중에 조정
 */
public class ShopService {

    private final Io io;

    public ShopService(Io io) {
        this.io = io;
    }

    public void open(GameSession session) {
        List<Consumables> stock = rollStock(session, 6);

        while (true) {
            ConsoleUi.clearConsole();
            System.out.println("\n[상점] 보유 골드: " + Math.round(session.getGold())+"골드");
            System.out.println("1. 구매  2. 판매  3. 나가기");
            int cmd = io.readInt(">>>", 3);

            if (cmd == 1) {
                buyMenu(session, stock);
            } else if (cmd == 2) {
                sellMenu(session);
            } else {
                return;
            }
        }
    }

    private List<Consumables> rollStock(GameSession session, int size) {
        List<Consumables> all = new ArrayList<>(session.allConsumables());
        Collections.shuffle(all, session.rng());
        return all.stream().limit(size).collect(Collectors.toList());
    }

    // 상점에서 상품 목록을 출력하고 구매를 담당하는 메소드
    private void buyMenu(GameSession session, List<Consumables> stock) {
        while (true) {
            ConsoleUi.clearConsole();
            System.out.println("\n[상점] 보유 골드: " + Math.round(session.getGold())+"골드");
            System.out.println("\n[구매] (0 입력: 뒤로)");
            for (int i = 0; i < stock.size(); i++) {
                Consumables c = stock.get(i);
                System.out.println((i + 1) + ") " + c.getName() + " - " + Math.round(c.getPrice()) + "골드");
            }
            System.out.println("구매할 번호를 고르세요.");

            int input = io.readIntAllowZero(">>>", stock.size());
            if (input == 0) return;

            Consumables chosen = stock.get(input - 1);
            double price = chosen.getPrice();
            if (session.getGold() < price) {
                System.out.println("골드가 부족합니다.");
                io.anythingToContinue();
                continue;
            }
            session.addGold(-price);
            session.addItem(chosen.getName(), 1);
            System.out.println("구매 완료: " + chosen.getName());
            io.anythingToContinue();
        }
    }

    private void sellMenu(GameSession session) {
        while (true) {
            ConsoleUi.clearConsole();
            var inv = session.inventoryView();
            if (inv.isEmpty()) {
                System.out.println("\n[판매] 인벤토리가 비었습니다.");
                io.anythingToContinue();
                return;
            }

            ConsoleUi.clearConsole();
            System.out.println("\n[상점] 보유 골드: " + Math.round(session.getGold())+"골드");
            System.out.println("\n[판매] (0 입력: 뒤로)");
            List<String> names = new ArrayList<>(inv.keySet());
            for (int i = 0; i < names.size(); i++) {
                String name = names.get(i);
                int count = inv.get(name);
                double unit = sellPrice(session, name);
                System.out.println((i + 1) + ") " + name + " x" + count + " - 개당 " + Math.round(unit) + "골드");
            }
            System.out.println("판매할 아이템 번호를 고르세요.");

            int input = io.readIntAllowZero(">>>", names.size());
            if (input == 0) return;

            String name = names.get(input - 1);
            if (!session.removeItem(name, 1)) {
                System.out.println("판매 실패");
                io.anythingToContinue();
                continue;
            }
            double gain = sellPrice(session, name);
            session.addGold(gain);
            System.out.println("판매 완료: " + name + " (" + Math.round(gain) + "골드)");
            io.anythingToContinue();
        }
    }

    private double sellPrice(GameSession session, String itemName) {
        Consumables def = session.consumableDef(itemName);
        double base = (def == null) ? 10 : def.getPrice();
        return Math.max(1, Math.floor(base * 0.5));
    }
}
