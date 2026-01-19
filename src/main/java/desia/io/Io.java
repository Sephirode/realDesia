package desia.io;

import java.util.Scanner;

public class Io {
    Scanner scan = new Scanner(System.in);
    public int readInt(String prompt,int userChoices) {

        int input;

        do{
            System.out.print(prompt+" ");
            try {
                input=Integer.parseInt(scan.next());
                scan.nextLine();    //버퍼에 남은 찌꺼기(특히 엔터)를 제거한다.
            }catch(Exception e) {
                input= -1;
                System.out.println("잘못된 입력입니다.");
            }
        }while(input < 1 || input > userChoices);
        return input;
    }

    public String readNonEmptyString(String prompt, int maxLen) {
        while(true) {
            System.out.print(prompt + " ");
            String s = scan.nextLine();
            if (s == null){
                System.out.println("잘못된 입력입니다.");
                continue;
            }
            s = s.strip();  //문자열의 앞뒤 유니코드 공백을 전부 제거한다. trim() 함수는 \u0020 이하의 문자만 제거한다.
            if(s.isEmpty()) {
                System.out.println("이름에 공백은 불가합니다.");
                continue;
            }
            if(s.length()>maxLen){
                System.out.println("이름은 "+maxLen+"자 이하로만 설정 가능합니다.");
                continue;
            }
            return s;
        }
    }

    // 플레이어가 누르기 전까지 대기하게 하는 메소드
    public void anythingToContinue() {
        System.out.print("\n\n계속하려면 아무 키나 입력하세요...");
        scan.next();
    }

    public boolean confirmExit() {
        Io io1 = new Io();
        System.out.println("게임을 종료하시겠습니까?\n(Y: 1 / N: 2)");
        int input = io1.readInt(">>>", 2);
        return input == 1; // 1이면 종료, 2면 취소
    }

}
