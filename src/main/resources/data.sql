-- 사용자 샘플 데이터 추가
INSERT INTO users (username, name, password, email, phone, card_number, admin_level) VALUES ('testuser1', '김테스트', 'pass1234', 'test1@example.com', '010-1111-1111', '1234-1234-1234-1234', 0);
INSERT INTO users (username, name, password, email, phone, card_number, admin_level) VALUES ('testuser2', '이테스트', 'pass5678', 'test2@example.com', '010-2222-2222', '5678-5678-5678-5678', 0);
INSERT INTO users (username, name, password, email, phone, card_number, admin_level) VALUES ('admin1', '관리자', 'adminpass', 'admin@example.com', '010-9999-9999', '9999-9999-9999-9999', 1);
