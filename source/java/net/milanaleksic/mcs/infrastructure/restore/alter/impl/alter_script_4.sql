ALTER TABLE DB2ADMIN.FILM DROP COLUMN IMDBREJTING;

ALTER TABLE DB2ADMIN.FILM ADD COLUMN IMDB_ID varchar(10) DEFAULT '';

UPDATE DB2ADMIN.PARAM SET Value = '4' WHERE Name = 'VERSION';
