insert into DB2ADMIN.TipMedija(Naziv)
(select 'CD' WHERE 1 NOT IN (SELECT 1 FROM DB2ADMIN.TipMedija WHERE Naziv='CD'));

insert into DB2ADMIN.TipMedija(Naziv)
(select 'DVD' WHERE 1 NOT IN (SELECT 1 FROM DB2ADMIN.TipMedija WHERE Naziv='DVD'));

alter table DB2ADMIN.POZICIJA add column defaultPosition CHAR(1) DEFAULT 'N';

update DB2ADMIN.POZICIJA set defaultPosition='Y' where pozicija = 'присутан';

UPDATE DB2ADMIN.PARAM SET Value = '6' WHERE Name = 'VERSION';
