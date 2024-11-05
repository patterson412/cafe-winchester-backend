use `cafe-winchester`;

drop table if exists `roles`;
drop table if exists `members`;

create table `members` (
    `user_id` varchar(50) not null,
    `pw` char(68) not null,
    `active` tinyint not null,
    primary key (`user_id`)
) engine=InnoDB default charset=latin1;

insert into `members`
values
    ('patterson', '{bcrypt}$2a$10$9YB/i7tf/Ge9E3NPNVBqj.WCpZF82OTUAY1HCKOpyaChtDDyLlwY.', 1),
    ('manager', '{bcrypt}$2a$10$9YB/i7tf/Ge9E3NPNVBqj.WCpZF82OTUAY1HCKOpyaChtDDyLlwY.', 1);

create table `roles` (
    `user_id` varchar(50) not null,
    `role` varchar(50) not null,
    unique key `authorities5_idx_1` (`user_id`,`role`),
    constraint `authorities5_ibfk_1` foreign key (`user_id`) references `members` (`user_id`)
) engine=InnoDB default charset=latin1;

insert into `roles`
values
    ('patterson', 'ROLE_USER'),
    ('manager', 'ROLE_ADMIN');

