package desia.io;

import java.util.Scanner;

/** Console implementation (Scanner 기반). */
public class ConsoleIo implements Io {
    private final Scanner scan = new Scanner(System.in);

    @Override
    public int readInt(String prompt, int userChoices) {
        while (true) {
            System.out.print(prompt + " ");
            String line = scan.nextLine();
            try {
                int v = Integer.parseInt(line.strip());
                if (v >= 1 && v <= userChoices) return v;
            } catch (Exception ignored) {}
            System.out.println("잘못된 입력입니다.");
        }
    }

    @Override
    public int readIntAllowZero(String prompt, int max) {
        while (true) {
            System.out.println(prompt + " ");
            String line = scan.nextLine();
            try {
                int v = Integer.parseInt(line.strip());
                if (v >= 0 && v <= max) return v;
            } catch (Exception ignored) {}
            System.out.println("잘못된 입력. 양의 정수를 입력하시오.");
        }
    }

    @Override
    public int choose(String prompt, java.util.List<String> options) {
        if (options == null || options.isEmpty()) {
            System.out.println("선택지가 없습니다.");
            return 1;
        }
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        return readInt(prompt, options.size());
    }

    @Override
    public int chooseAllowCancel(String prompt, java.util.List<String> options, String cancelLabel) {
        if (options == null) options = java.util.List.of();
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
        System.out.println("0. " + (cancelLabel == null ? "취소" : cancelLabel));
        return readIntAllowZero(prompt, options.size());
    }

    @Override
    public String readNonEmptyString(String prompt, int maxLen) {
        while (true) {
            System.out.print(prompt + " ");
            String s = scan.nextLine();
            if (s == null) {
                System.out.println("문자열을 입력해주세요.");
                continue;
            }
            s = s.strip();
            if (s.isEmpty()) {
                System.out.println("공백만 입력할 수 없습니다.");
                continue;
            }
            if (s.length() > maxLen) {
                System.out.println(maxLen + "자 이하로만 입력 가능합니다.");
                continue;
            }
            return s;
        }
    }

    @Override
    public void anythingToContinue() {
        System.out.print("\n\n계속하려면 아무 키나 입력하세요...");
        scan.nextLine();
    }

    @Override
    public boolean confirmExit() {
        return confirm("게임을 종료하시겠습니까?", "예", "아니오");
    }
}
