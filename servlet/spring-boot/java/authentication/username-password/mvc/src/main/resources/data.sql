CREATE TABLE CUSTOM_USER (
    id VARCHAR(50) PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL
);

insert into CUSTOM_USER (id,email,password) values ('rob','rob@example.com','{bcrypt}$2a$10$2.kPE75Cxl6NXMsPfDXFLOGdMGT8tqXLk8J7S.uYcDfLCIBy/dpCq');
insert into CUSTOM_USER (id,email,password) values ('luke','luke@example.com','{bcrypt}$2a$10$2.kPE75Cxl6NXMsPfDXFLOGdMGT8tqXLk8J7S.uYcDfLCIBy/dpCq');

COMMIT;