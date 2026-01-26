package desia.io;

import java.util.Scanner;

public class Io {
    private final Scanner scan = new Scanner(System.in);

    // 1~userChoices 범위의 정수를 읽는 메소드. next() 혼용을 피하기 위해 항상 nextLine()으로 동작한다.
    public int readInt(String prompt,int userChoices) {
        while(true) {
            System.out.print(prompt + " ");
            String line = scan.nextLine();
            try {
                int v = Integer.parseInt(line.strip()); //버퍼에 남은 찌꺼기(특히 엔터)를 제거한다.
                // 제대로 된 입력을 받았을 경우, v를 그대로 리턴
                if (v >= 1 && v <= userChoices)
                    return v;
            } catch (Exception ignored) {}
                System.out.println("잘못된 입력입니다.");
        }
    }

    // 0 이상의 정수를 읽는 메소드. 뒤로가기, 취소 기능이 탑재되어있다.
    public int readIntAllowZero(String prompt, int max) {
        while (true) {
            System.out.println(prompt + " ");
            String line = scan.nextLine();
            try {
                int v = Integer.parseInt(line.strip());
                if (v >=0 && v <= max)
                    return v;
            } catch (Exception ingnored) {}
            System.out.println("잘못된 입력. 양의 정수를 입력하시오.");
        }
    }

    // 순수 공백을 제외한 문자열을 읽는 메소드.
    public String readNonEmptyString(String prompt, int maxLen) {
        while(true) {
            System.out.print(prompt + " ");
            String s = scan.nextLine();
            if (s == null){
                System.out.println("문자열을 입력해주세요.");
                continue;
            }
            s = s.strip();  // strip()함수는 문자열의 앞뒤 유니코드 공백을 전부 제거한다. trim() 함수는 \u0020 이하의 문자만 제거한다.
            if(s.isEmpty()) {
                System.out.println("공백만 입력할 수 없습니다.");
                continue;
            }
            if(s.length()>maxLen){
                System.out.println(maxLen + "자 이하로만 입력 가능합니다.");
                continue;
            }
            return s;
        }
    }

    // 플레이어가 누르기 전까지 대기하게 하는 메소드
    public void anythingToContinue() {
        System.out.print("\n\n계속하려면 아무 키나 입력하세요...");
        scan.nextLine();
    }
    //게임 종료 여부를 묻는 메소드
    public boolean confirmExit() {
        System.out.println("게임을 종료하시겠습니까?\n(Y: 1 / N: 2)");
        return
                readInt(">>>", 2) == 1; // 1이면 종료(true), 2면 취소(false).
    }

}
