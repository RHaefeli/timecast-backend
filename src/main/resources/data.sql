INSERT INTO employee(last_name, first_name, email_Address, role, active, password) VALUES
('John', 'Miller', 'admin@timecast.ch', 'ADMINISTRATOR', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Marco', 'Peter', 'marpet@tiemcast.ch', 'PROJECTMANAGER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Christen', 'Hans', 'chrans@timecast.ch', 'PROJECTMANAGER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Bombadil', 'Tom', 'tombadil@timecast.ch', 'PROJECTMANAGER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Beutlin', 'Frodo', 'fordbeu@timecast.ch', 'PROJECTMANAGER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Weise', 'Gandalf', 'gandweis@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Haller', 'Martin', 'marhalle@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Müller', 'Lukas', 'lukatoni@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Würgler', 'Samuel', 'samwuegler@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Hunziker', 'David', 'davihunzi@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Odermatt', 'Nicolas', 'nicoderma@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Yilmaz', 'Osman', 'osmayilm@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Frauendiener', 'Jannick', 'jannfrudi@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Schürch', 'Robin', 'schuerob@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Özyurt', 'Cagatay', 'cagatozyurt@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Ivanovik', 'Dimitri', 'dimivanov@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Nüesch', 'Lucien', 'lucuesch@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Mirco', 'Ivan', 'ivanmirc@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Ruedi', 'Jürg', 'jurguedi@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Müller', 'Marco', 'marcmuell@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha'),
('Dietrich', 'Hans', 'handiet@timecast.ch', 'DEVELOPER', true, '$2a$10$6VpQNR89YEB5xez0blGgd.eL8JZiIj08xMfWPJCXD4qKVlzKhGgha');

INSERT INTO project(name, fte_percentage, start_date, end_date, project_manager_id) VALUES
('aWall', 300, '2016-01-01', '2021-12-31', 2),
('Bobbahn', 500, '2017-01-01', '2022-12-31', 3),
('Half Life 3', 400, '2020-01-01', '2030-12-31', 4),
('Reddit clone', 500, '2015-01-01', '2018-12-31', 5);

INSERT INTO contract(employee_id, pensum_percentage, start_date, end_date) VALUES
(2, 100, '2014-01-01', '2020-12-31'),
(3, 100, '2014-01-01', '2020-12-31'),
(4, 100, '2014-01-01', '2020-12-31'),
(5, 100, '2014-01-01', '2020-12-31'),
(6, 100, '2015-01-01', '2021-12-31'),
(7, 100, '2015-01-01', '2021-12-31'),
(8, 100, '2015-01-01', '2021-12-31'),
(9, 100, '2015-01-01', '2021-12-31'),
(10, 100, '2016-01-01', '2022-12-31'),
(11, 100, '2016-01-01', '2022-12-31'),
(12, 100, '2016-01-01', '2022-12-31'),
(13, 100, '2016-01-01', '2022-12-31'),
(14, 100, '2017-01-01', '2023-12-31'),
(15, 100, '2017-01-01', '2023-12-31'),
(16, 100, '2017-01-01', '2023-12-31'),
(17, 100, '2017-01-01', '2023-12-31'),
(18, 100, '2015-01-01', '2017-12-31'),
(19, 100, '2014-01-01', '2017-12-31');

INSERT INTO allocation(project_id, contract_id, pensum_percentage, start_date, end_date) VALUES
(4, 2, 100, '2015-01-01', '2018-12-31'),
(4, 6, 100, '2015-01-01', '2018-12-31'),
(4, 7, 100, '2015-01-01', '2018-12-31'),
(4, 8, 100, '2015-01-01', '2018-12-31'),
(4, 9, 100, '2015-01-01', '2018-12-31'),
(1, 3, 100, '2016-01-01', '2021-12-31'),
(1, 10, 100, '2016-01-01', '2021-12-31'),
(1, 11, 50, '2016-01-01', '2021-12-31'),
(1, 12, 50, '2016-01-01', '2021-12-31'),
(2, 4, 100, '2017-01-01', '2022-12-31'),
(2, 13, 80, '2017-01-01', '2022-12-31'),
(2, 14, 80, '2017-01-01', '2022-12-31'),
(2, 15, 40, '2017-01-01', '2022-12-31'),
(2, 16, 50, '2017-01-01', '2022-12-31'),
(2, 17, 50, '2017-01-01', '2022-12-31');
