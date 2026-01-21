# ZDME

공유 모빌리티 백엔드 프로젝트

## 🛠 Tech Stack
- **Framework:** Spring Boot 3.5.9
- **Language:** Java 17
- **Database:** Oracle Database 21c Express Edition (XE)
- **ORM:** Spring Data JPA

---

## ⚙️ 로컬 초기 환경 설정 (Local Setup)

### 1. Oracle Database 21c 설정
Oracle XE 설치 후 `sqlplus`를 통해 프로젝트 전용 PDB(Pluggable Database)와 계정을 생성

```sql
-- 1. 관리자 권한 접속 후 PDB(XEPDB1)로 세션 변경
ALTER SESSION SET CONTAINER = XEPDB1;

-- 2. 사용자 계정 생성 (아이디/비번: zdme)
CREATE USER zdme IDENTIFIED BY zdme;

-- 3. 권한 부여 및 저장소 할당
GRANT CONNECT, RESOURCE TO zdme;
ALTER USER zdme QUOTA UNLIMITED ON USERS;
```
### 2. application.properties 설정
본인 환경에 맞춰서 수정이 필요할 시 해당 파일 수정하여 database 설정

