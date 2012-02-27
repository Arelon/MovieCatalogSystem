alter table DB2ADMIN.POZICIJA add column defaultPosition CHAR(1) DEFAULT 'N';

update DB2ADMIN.POZICIJA set defaultPosition='Y' where pozicija = 'присутан';

UPDATE DB2ADMIN.PARAM SET Value = '6' WHERE Name = 'VERSION';
