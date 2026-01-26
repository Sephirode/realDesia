package desia.ui;

/*
 * 콘솔 출력 전용 유틸.
 *
 * 기존 구조에서는 Battle/Shop/Story 등 여러 클래스가 `new Game()`를 만들어
 * clearConsole/printSeparator 같은 출력 헬퍼만 사용했는데,
 * 이때 Game 내부에서 Io(Scanner)까지 새로 생성되어 입력 버그/스파게티가 발생한다.
 *
 * 출력은 여기로 모으고, 입력(Io)은 "단 1개"만 공유하도록 구조를 정리한다.
 */
public class ConsoleUi {
    private ConsoleUi() {}

    // 콘솔 창에 줄바꿈을 반복해서 청소하는(것처럼 보이게 하는) 메소드
    public static void clearConsole() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    // 길이가 n인 문단 구분자 만들기 - 2개 버전 (----- / =====)
    public static void printSeparator(int n) {
        for (int i = 0; i < n; i++) System.out.print("-");
        System.out.println();
    }
    public static void printSeparatorX(int n) {
        for (int i = 0; i < n; i++) System.out.print("=");
        System.out.println();
    }

    /**
     * n==1: "-" 구분선, n==2: "=" 구분선
     */
    public static void printHeading(String title, int n) {
        if (n == 2) {
            printSeparatorX(30);
            System.out.println(title);
            printSeparatorX(30);
            return;
        }
        printSeparator(30);
        System.out.println(title);
        printSeparator(30);
    }
}
