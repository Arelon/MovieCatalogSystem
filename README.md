Movie Catalog System
=======

Movie catalog creation and maintenance desktop application written in Java.
------------------------------------------------------------


Movie Catalog System (MCS) is an open source Windows application for maintaining your movie collection.

It major qualities are, before all else, quick movie search and medium handling.

The idea behind the program is that all you movies are either on DVDs or CDs. Numbering of the mediums
is critical, but application has integrated id generator to ease the pain of this boring job.
Every movie can take one or more mediums, can have one (and only one) genre.
Each medium can positioned on a single location (whereas location can be a name of a person you
borrowed the medium to). Entire application interface is on Serbian (sorry), but if you wish you
can fork the project and work on a translation to any language.

In case you wish to have a simple movie list so you can print it out or send via email, you can do
that - MCS has an export feature that creates a dynamic HTML file in which you can select items and
aggregate CDs/DVDs you want to take, to ease the borrowing process.

"Movie Catalog System" is application that went through a very long way from the initial version
(which was ASP .Net / MSSQL application) and now is a Spring/SWT/Hibernate Windows application
with support for Derby/DB2/HSQLDB/H2 databases. Every time you close the program, complete SQL
dump is created so you can import the database to some other DB of choice.
MCS is a portable application (you can start it from USB or keep it in your Dropbox folder).

The only precondition is **Windows** and **Java Runtime 1.6**.

(c) 2009-2011 by Milan Aleksic.
