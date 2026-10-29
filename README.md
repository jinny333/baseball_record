# baseball_record

야구 직관 기록(일기) 백엔드 API. 티켓 OCR로 경기 정보를 자동 인식하고, 직관 기록/리뷰를 관리하며 팀별·구장별 승률 통계와 좌석 추천을 제공한다.

## Tech Stack

- Java 21, Spring Boot 4.1.0 (Gradle)
- Spring Data JPA + MySQL
- Spring Security + JWT (Access/Refresh), 카카오/네이버 소셜 로그인
- KOMORAN (한국어 형태소 분석) — 리뷰 분석/좌석 추천
- Google Cloud Vision API — 티켓 OCR 파싱

## 로컬 실행

```bash
./gradlew bootRun
```

MySQL이 `localhost:3306`에 떠 있어야 하며(DB `baseball_record`), 아래 환경변수로 접속 정보를 오버라이드할 수 있다.

| 환경변수 | 설명 | 기본값 |
| --- | --- | --- |
| `DB_HOST` / `DB_PORT` / `DB_NAME` | MySQL 접속 정보 | `localhost` / `3306` / `baseball_record` |
| `DB_USERNAME` / `DB_PASSWORD` | MySQL 계정 | `root` / (빈 값) |
| `JWT_SECRET` | JWT 서명 키 (운영 환경에서는 반드시 변경, 32byte 이상) | 개발용 기본값 |
| `VISION_API_KEY` | Google Cloud Vision API 키 (비어있으면 OCR은 실패하고 수동 입력으로 폴백) | (빈 값) |

## 빌드 / 테스트

```bash
./gradlew build
```

`RecordApplicationTests`(`@SpringBootTest`)는 실제 MySQL 연결이 필요하다. 로컬에 DB가 없으면 테스트만 제외하고 빌드:

```bash
./gradlew build -x test
```

## API 문서 (Swagger)

서버 실행 후 `http://localhost:8080/swagger-ui/index.html` 에서 전체 API 스펙 확인 및 테스트 가능. 인증이 필요한 API는 `/api/v1/auth/login`으로 발급받은 Access Token을 우측 상단 `Authorize` 버튼에 `Bearer <token>` 형식으로 넣으면 된다.

## Docker

```bash
docker build -t baseball-record .
docker run -p 8080:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=yourpassword \
  -e JWT_SECRET=change-this-to-a-real-32-byte-secret \
  baseball-record
```

## CI

`.github/workflows/ci.yml`에서 push/PR 시 MySQL 서비스 컨테이너를 띄워 빌드·테스트를 수행하고, Dockerfile 빌드를 검증한다.
