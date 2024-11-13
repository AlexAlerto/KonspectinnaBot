# Konspectinna

## Description

Konspectinna - Is telegram bot for students (and beyond) to view educational files of their educational stream.

This repository contains the entire main part of the project.



## Local access

For local launch and development you will need:

- [Java 17+](https://www.oracle.com/java/)
- [Git](https://git-scm.com/)
- [MySQL](https://dev.mysql.com/)
- [Telegram bot](https://t.me/BotFather)

### Preparation and first launch
1. Clone yourself a repository
2. In the `src/main/resources` folder, create an `application.properties` file
3. Install MySQL, create a database
4. Create Telegram Bot
5. In the file `application.properties` designate the necessary settings for operation (example and description of settings see README below )
6. Forward the necessary ports
7. Run class `TgBotApplication`

### `application.properties` file
Includes:
- `server.port` - web service port (usually `8080`)
- `bot.token` - token of the bot you created
- `spring.jpa.hibernate.ddl-auto` - usually `update`
- `spring.datasource.url` - link to your database (example: `jdbc:mysql://127.0.0.1:3306/konspectinna`)
- `spring.datasource.username` - username in your database
- `spring.datasource.password` - user password in your database
- `spring.datasource.driver-class-name` - usually `com.mysql.cj.jdbc.Driver`
