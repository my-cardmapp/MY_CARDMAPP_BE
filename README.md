# Card-Map Backend API

백엔드 API 서버 - 복지카드 및 지역화폐 가맹점 검색 서비스

## 🛠 Tech Stack

- **Java 21** with Virtual Threads
- **Spring Boot 3.2**
- **PostgreSQL 16** + PostGIS 3.5
- **Redis 7** (Caching & Session)
- **Spring Data JPA**
- **Spring Security**
- **Swagger/OpenAPI 3.0**

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/thc/my_cardmapp/
│   │   ├── config/         # 설정 (Security, Redis, DB 등)
│   │   ├── controller/     # REST API 컨트롤러
│   │   ├── domain/         # JPA 엔티티
│   │   ├── dto/            # 데이터 전송 객체
│   │   ├── repository/     # JPA Repository
│   │   └── service/        # 비즈니스 로직
│   └── resources/
│       ├── application.yml              # 기본 설정
│       ├── application-local.yml        # 로컬 환경
│       ├── application-prod.yml         # 프로덕션 환경
│       └── data.sql                     # 초기 데이터
└── test/                   # 테스트 코드
```

## 🚀 Running Locally

### Prerequisites

- Java 21
- Docker & Docker Compose
- Gradle

### 1. Start PostgreSQL & Redis

```bash
docker-compose up -d
```

### 2. Run Application

```bash
./gradlew bootRun
```

서버가 http://localhost:8080 에서 실행됩니다.

### 3. API Documentation

Swagger UI: http://localhost:8080/swagger-ui.html

Health Check: http://localhost:8080/health

## 📦 Build

```bash
# 빌드
./gradlew build

# 테스트 제외 빌드
./gradlew build -x test

# JAR 파일 실행
java -jar build/libs/my-cardmapp-0.0.1-SNAPSHOT.jar
```

## 🐳 Docker

```bash
# Docker 이미지 빌드
docker build -t cardmap-backend .

# 컨테이너 실행
docker run -p 8080:8080 cardmap-backend
```

## 🌐 Deployment

현재 [Render](https://render.com)에 배포 중입니다.

배포 설정: `render.yaml`

## 🔑 Environment Variables

```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/cardmap
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=postgres

# Redis
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# CORS (Production)
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com

# OAuth (Optional)
KAKAO_CLIENT_ID=your_kakao_client_id
NAVER_CLIENT_ID=your_naver_client_id
```

## 📋 API Endpoints

### Cards

- `GET /api/v1/cards` - 카드 목록 조회
- `GET /api/v1/cards/{code}` - 카드 상세 조회

### Merchants

- `GET /api/v1/merchants` - 가맹점 목록 조회
- `GET /api/v1/merchants/{id}` - 가맹점 상세 조회
- `GET /api/v1/merchants/nearby` - 주변 가맹점 검색
- `GET /api/v1/merchants/search` - 가맹점 텍스트 검색

### Health

- `GET /health` - 서버 상태 확인

## 🧪 Testing

```bash
# 모든 테스트 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests EntityCreationTest
```

## 📝 Database Schema

주요 엔티티:

- **Card**: 복지카드 정보 (아동급식카드, 문화누리카드 등)
- **Merchant**: 가맹점 정보 (이름, 주소, 위치, 카테고리)
- **Category**: 업종 분류 (편의점, 음식점 등)
- **User**: 사용자 정보 (OAuth 로그인)
- **MerchantCard**: 가맹점-카드 연결 테이블

PostGIS 사용으로 위치 기반 검색 지원

## 🔧 Configuration Profiles

- **local**: 로컬 개발 환경 (H2 또는 로컬 PostgreSQL)
- **prod**: 프로덕션 환경 (외부 DB, Redis)

프로필 전환:

```bash
# 로컬 실행
./gradlew bootRun --args='--spring.profiles.active=local'

# 프로덕션 실행
./gradlew bootRun --args='--spring.profiles.active=prod'
```

## 📖 Related Repositories

- Frontend: [MY_CARDMAPP](https://github.com/my-cardmapp/MY_CARDMAPP)

## 📄 License

This project is licensed under the MIT License.
