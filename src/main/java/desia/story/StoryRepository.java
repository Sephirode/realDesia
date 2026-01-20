package desia.story;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

/*
 * story.json: { "key": "문구...", ... }
 * 개발자는 story.json만 수정해서 스토리/이벤트 문구를 바꿀 수 있음.
 */
public class StoryRepository {

    private final Map<String, String> stories;

    public StoryRepository() {
        this.stories = Collections.unmodifiableMap(load());
    }

    public String get(String key) {
        String v = stories.get(key);
        if (v == null) return "(스토리 키 없음: " + key + ")";
        return v;
    }

    private Map<String, String> load() {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("story.json")) {
            if (in == null) throw new RuntimeException("story.json 리소스가 발견되지 않음");
            ObjectMapper om = new ObjectMapper();
            return om.readValue(in, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("story.json 로딩 실패: " + e.getMessage(), e);
        }
    }
}
