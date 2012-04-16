Movie Catalog System
=======

Movie catalog creation and maintenance Windows application
------------------------------------------------------------

Movie Catalog System (MCS) is an open source Windows application for maintaining your movie collection.

The idea behind the program is that all your movies are on some "mediums" (CD, DVD, hard drive etc.). 
Numbering of the mediums is critical, but application has integrated id generator to ease the pain 
of this boring job.
Every movie can take one or more mediums, can have one (and only one) genre - for now.
Each medium can positioned on a single location (whereas location can be a name of a person you
borrowed the medium to). Entire application interface is I18N-ed, with English and Serbian Cyrillic interfaces
supported out-of-the-box. New translations are welcome (just add new messages.properties for your locale).

In case you wish to have a simple movie list so you can print it out or send via email, you can do
that - MCS has an export feature that creates a dynamic HTML file in which you can select items and
aggregate CDs/DVDs you want to take, to ease the borrowing process.

Every time you close the program, complete SQL dump is created so you can import the database to some
other DB of choice.
MCS is a portable application (you can start it from USB or keep it in your Dropbox folder).

The only preconditions are **Windows** and **Java Runtime 7**.

(c) 2009-2012 by Milan Aleksic.
