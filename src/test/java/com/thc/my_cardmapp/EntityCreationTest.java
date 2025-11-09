package com.thc.my_cardmapp;

import com.thc.my_cardmapp.domain.User;
import com.thc.my_cardmapp.domain.Card;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class EntityCreationTest {

    @Test
    void contextLoads() {
        // 애플리케이션이 정상 실행되면 통과
    }

    @Test
    void testEntityCreation() {
        // 엔티티 객체 생성 테스트
        User user = User.builder()
                .email("test@test.com")
                .passwordHash("hash")
                .nickname("테스트")
                .build();

        assert user.getEmail().equals("test@test.com");
    }
}