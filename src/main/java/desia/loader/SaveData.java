package desia.loader;

import lombok.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 세이브 파일 1개에 해당하는 데이터 모델.
 * - JSON 직렬화/역직렬화용 DTO
 * - "정의 데이터"(Player/Enemy/Consumables)는 저장하지 않고, 로드 시 다시 JSON에서 읽는다.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SaveData {
    // 파일 포맷 버전(호환성용)
    private int version = 1;

    // 플레이어 식별
    private String playerClass;   // 예: "전사" (Player.classes)
    private String playerName;    // 닉네임

    // 진행도
    private int chapter;
    private int act;

    // 성장
    private int level;
    private double exp;

    // 현재 자원
    private double hp;
    private double mp;

    // 재화
    private double gold;

    // 인벤토리(아이템 이름 -> 개수)
    @Builder.Default
    private Map<String, Integer> inventory = new LinkedHashMap<>();
}
