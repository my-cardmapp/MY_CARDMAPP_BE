-- Card-Map 테스트 데이터
-- 카드, 카테고리, 가맹점 테스트 데이터 삽입

-- ============================================
-- 1. 카드 정보
-- ============================================
INSERT INTO card (name, issuer, color_hex) VALUES
('아동급식카드', '보건복지부', '#FF6B6B'),
('문화누리카드', '문화체육관광부', '#4ECDC4'),
('지역사랑상품권', '행정안전부', '#95E1D3')
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- 2. 카테고리 정보
-- ============================================
INSERT INTO category (name) VALUES
('음식점'),
('편의점'),
('카페'),
('베이커리'),
('서점'),
('영화관'),
('마트'),
('일반상점')
ON CONFLICT (name) DO NOTHING;

-- ============================================
-- 3. 가맹점 정보 (서울 강남 지역)
-- ============================================

-- 아동급식카드 가맹점
INSERT INTO merchant (name, category_id, address, location) VALUES
('강남 CU편의점', (SELECT id FROM category WHERE name = '편의점'), '서울특별시 강남구 테헤란로 123', ST_SetSRID(ST_MakePoint(127.0276, 37.4979), 4326)),
('역삼 GS25', (SELECT id FROM category WHERE name = '편의점'), '서울특별시 강남구 역삼동 456', ST_SetSRID(ST_MakePoint(127.0312, 37.5012), 4326)),
('강남역 김밥천국', (SELECT id FROM category WHERE name = '음식점'), '서울특별시 강남구 강남대로 789', ST_SetSRID(ST_MakePoint(127.0279, 37.4975), 4326)),
('역삼 파리바게뜨', (SELECT id FROM category WHERE name = '베이커리'), '서울특별시 강남구 역삼로 234', ST_SetSRID(ST_MakePoint(127.0298, 37.5001), 4326));

-- 문화누리카드 가맹점
INSERT INTO merchant (name, category_id, address, location) VALUES
('교보문고 강남점', (SELECT id FROM category WHERE name = '서점'), '서울특별시 강남구 강남대로 465', ST_SetSRID(ST_MakePoint(127.0285, 37.5015), 4326)),
('CGV 강남', (SELECT id FROM category WHERE name = '영화관'), '서울특별시 강남구 강남대로 438', ST_SetSRID(ST_MakePoint(127.0275, 37.4984), 4326)),
('영풍문고 강남점', (SELECT id FROM category WHERE name = '서점'), '서울특별시 강남구 역삼동 567', ST_SetSRID(ST_MakePoint(127.0305, 37.5003), 4326));

-- 지역사랑상품권 가맹점
INSERT INTO merchant (name, category_id, address, location) VALUES
('강남 이마트', (SELECT id FROM category WHERE name = '마트'), '서울특별시 강남구 테헤란로 501', ST_SetSRID(ST_MakePoint(127.0302, 37.5022), 4326)),
('역삼 스타벅스', (SELECT id FROM category WHERE name = '카페'), '서울특별시 강남구 역삼동 678', ST_SetSRID(ST_MakePoint(127.0318, 37.4991), 4326)),
('강남 BBQ치킨', (SELECT id FROM category WHERE name = '음식점'), '서울특별시 강남구 테헤란로 345', ST_SetSRID(ST_MakePoint(127.0289, 37.5008), 4326)),
('역삼 다이소', (SELECT id FROM category WHERE name = '일반상점'), '서울특별시 강남구 역삼로 156', ST_SetSRID(ST_MakePoint(127.0295, 37.4996), 4326)),
('강남 롯데리아', (SELECT id FROM category WHERE name = '음식점'), '서울특별시 강남구 강남대로 567', ST_SetSRID(ST_MakePoint(127.0282, 37.4988), 4326)),
('역삼 세븐일레븐', (SELECT id FROM category WHERE name = '편의점'), '서울특별시 강남구 역삼동 789', ST_SetSRID(ST_MakePoint(127.0308, 37.5018), 4326));

-- ============================================
-- 4. 가맹점-카드 연결
-- ============================================

-- 아동급식카드 가맹점 연결
INSERT INTO merchant_card (merchant_id, card_id) VALUES
((SELECT id FROM merchant WHERE name = '강남 CU편의점'), (SELECT id FROM card WHERE name = '아동급식카드')),
((SELECT id FROM merchant WHERE name = '역삼 GS25'), (SELECT id FROM card WHERE name = '아동급식카드')),
((SELECT id FROM merchant WHERE name = '강남역 김밥천국'), (SELECT id FROM card WHERE name = '아동급식카드')),
((SELECT id FROM merchant WHERE name = '역삼 파리바게뜨'), (SELECT id FROM card WHERE name = '아동급식카드'));

-- 문화누리카드 가맹점 연결
INSERT INTO merchant_card (merchant_id, card_id) VALUES
((SELECT id FROM merchant WHERE name = '교보문고 강남점'), (SELECT id FROM card WHERE name = '문화누리카드')),
((SELECT id FROM merchant WHERE name = 'CGV 강남'), (SELECT id FROM card WHERE name = '문화누리카드')),
((SELECT id FROM merchant WHERE name = '영풍문고 강남점'), (SELECT id FROM card WHERE name = '문화누리카드'));

-- 지역사랑상품권 가맹점 연결 (다양한 업종)
INSERT INTO merchant_card (merchant_id, card_id) VALUES
((SELECT id FROM merchant WHERE name = '강남 이마트'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '역삼 스타벅스'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '강남 BBQ치킨'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '역삼 다이소'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '강남 롯데리아'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '역삼 세븐일레븐'), (SELECT id FROM card WHERE name = '지역사랑상품권'));

-- 일부 가맹점은 여러 카드 동시 사용 가능
INSERT INTO merchant_card (merchant_id, card_id) VALUES
((SELECT id FROM merchant WHERE name = '강남 CU편의점'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '역삼 GS25'), (SELECT id FROM card WHERE name = '지역사랑상품권')),
((SELECT id FROM merchant WHERE name = '교보문고 강남점'), (SELECT id FROM card WHERE name = '지역사랑상품권'));
