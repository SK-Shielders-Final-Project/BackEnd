-- 사용자 샘플 데이터 추가
INSERT INTO users (username, name, password, email, phone, admin_level) VALUES ('testuser1', '김테스트', '$2a$10$N.xnyV51z5v5f2jL7lVpA.SjFre4o22jrVzbuDx3h4/PjL2w0ND6O', 'test1@example.com', '010-1111-1111', 0);
INSERT INTO users (username, name, password, email, phone, admin_level) VALUES ('testuser2', '이테스트', '$2a$10$8.IdKM7/Q22aT.vnqHrL9uD.kU2yrzJSSu47wz1a4hPDCWcE8fC.S', 'test2@example.com', '010-2222-2222', 0);
INSERT INTO users (username, name, password, email, phone, admin_level) VALUES ('admin1', '관리자', '$2a$10$wL4P9g52.WpS9pM1aL4THeKFlY1wIVc8zVPIaH5z2vXg8x2j5/f.m', 'admin@example.com', '010-9999-9999', 1);
