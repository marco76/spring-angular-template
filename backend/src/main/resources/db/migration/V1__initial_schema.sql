create table app_setting (
    setting_key varchar(100) primary key,
    setting_value varchar(500) not null,
    created_at timestamp with time zone not null default current_timestamp
);

