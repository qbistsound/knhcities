# knhcities
Cities APP for K&amp;N

# configuration

The configuration file is located in `src/main/resources/config.json`.  Standard user and root passwords are included, root has edit access to the data and user interface.

```
{
	"database_username": "",
	"database_password": "",
	"database_namespace": "",
	"database_connections": 1,
	"database_url": "jdbc:sqlite:/tmp/knhcities.db",
	"authentication": true,
	"user_login": "user",
	"user_password": "user",
	"admin_login": "root",
	"admin_password": "toor" 
}
```
**database_username**       Username for the database.

**database_password**       Password for the database (optional).

**database_namespace**      Database Schema / Namespace (optional).

**database_connections**    Maximum number of connections ( must be >= 1) (Note SQLite does not support more than 1 connection).

**database_url**            JDBC Database connection string (ie. jdbc:postgresql:/[host]:[port]/[database-name] or jdbc:sqlite:/path/to/db).
  
**authentication**          Enables / Disables authentication.
  
# installation
  
  you can install the pre-cooked database from the provided files, `knhcities.db` is a sqlite file, but an additional `knhcities.sql` is also included.  If you use the sqlite driver, the system will create the datbase for you as long as it has write access to the provided database name, and all drivers support auto population, if the table does not exist but the database does. You can run through maven with `./mvnw spring-boot:run` from the source directory, or run the jar with the command `java -jar cities-0.0.1-SNAPSHOT.jar`.
  
  **[knhcities.sql]** -> https://www.dropbox.com/s/0fmozewkx4atc2v/knhcities.sql?dl=0
  
  **[knhcities.db]** -> https://www.dropbox.com/s/vjuugaqs8j3pi67/knhcities.db?dl=0
  
  **_notes:_**
  - if you do end up creating the database from scratch, this does consume a bit of time due to scraping all the images in the initial phase.
  - authentication is minimal, this is not the usual approach i would take, but for convenience and POC, authentication is done in-memory through HTTP Basic Authentication, normally an OAuth or custom WASP compatible authentication is preferred.
  - service.java is the vertical slice, and has no Spring Dependencies, allowing for the entire architecture to be moved to any container required.
  - there were plenty of missing images from the csv, i have alternated this with a `missing image` image.
