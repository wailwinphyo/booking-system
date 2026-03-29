-- Insert default packages
INSERT INTO packages (name, country, description, price, credits) VALUES
('Default Basic Package', 'Singapore', 'Default basic package', 10.0, 5),
('Default Premium Package', 'Singapore', 'Default premium package', 20.0, 10),
('Basic Package Myanmar', 'Myanmar', 'Basic package for Myanmar', 8.0, 4),
('Premium Package Myanmar', 'Myanmar', 'Premium package for Myanmar', 15.0, 8);

-- Insert default class schedules
INSERT INTO class_schedule (name, country, start_time, end_time, capacity, required_credits) VALUES
('Yoga Class', 'Singapore', '2026-04-01 10:00:00', '2026-04-01 11:00:00', 10, 1),
('Pilates Class', 'Singapore', '2026-04-01 14:00:00', '2026-04-01 16:00:00', 5, 2),
('Meditation Class', 'Singapore', '2026-04-02 09:00:00', '2026-04-02 10:00:00', 15, 1),
('HIIT Class', 'Singapore', '2026-04-02 18:00:00', '2026-04-02 19:00:00', 8, 2),
('Yoga Class', 'Myanmar', '2026-04-02 09:00:00', '2026-04-02 10:30:00', 8, 1),
('Pilates Class', 'Myanmar', '2026-04-02 13:00:00', '2026-04-02 14:30:00', 6, 2),
('Zumba Class', 'Myanmar', '2026-04-03 10:00:00', '2026-04-03 11:00:00', 12, 1),
('Strength Training', 'Myanmar', '2026-04-03 15:00:00', '2026-04-03 16:30:00', 7, 2);