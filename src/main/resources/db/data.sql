INSERT INTO t_user (username, password, email, phone, status) VALUES
('admin', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'admin@example.com', '13800000001', 1),
('user1', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'user1@example.com', '13800000002', 1),
('user2', '$2a$10$AmZ/1KpudYuXcVhypUlA6u9I9vM29xjDJmz3cZK9f9hI19e.6ENZ2', 'user2@example.com', '13800000003', 0);

INSERT INTO t_article (title, content, category, status, author_id) VALUES
('Spring Boot 入门', 'Spring Boot 是由 Pivotal 团队提供的全新框架...', '技术', 1, 1),
('Java 21 新特性', 'Java 21 引入了虚拟线程、模式匹配等新特性...', '技术', 1, 1),
('设计模式实践', '设计模式是软件开发中的最佳实践...', '编程', 0, 2);

INSERT INTO t_product (name, description, price, stock, status) VALUES
('机械键盘', 'Cherry MX 青轴，87键', 399.00, 100, 1),
('无线鼠标', '蓝牙5.0，静音按键', 129.00, 200, 1),
('显示器支架', '双屏支架，承重8kg', 259.00, 50, 1);
