create table Categories (
    label               varchar(200),
    colour              varchar(20) not null,
    primary key (label, colour)
);

create table Entries (
    category            references Categories(label),
    entry_description   varchar(200) not null,
    total_duration      double not null
);

create table Tasks (
    task_title          varchar(200) not null,
    task_description    varchar(200) not null,
    doDate              varchar(30) not null,
    dueDate             varchar(30) not null,
    task_priority       int not null
);