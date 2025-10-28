# testlab

## API Specs

- [Swagger File](./src/main/resources/webroot/swagger/swagger.yaml)


## Database Schema

- [DB Schema](./src/main/resources/db/mysql/schema.sql)

## Running Application

### Requirements

- java (version >= 17)
- maven

#### Running jar

- create jar using `mvn clean package`
- navigate to the jar directory by `cd target/testlab`
- Run the jar using command
  ```shell
  MYSQL_USER=<mysql-username> \
  MYSQL_PASSWORD=<mysql-password> \
  java -Dapp.environment=<env-type> -Dlogback.configurationFile=./resources/logback/logback.xml -jar testlab-<artifact-version>-fat.jar
  ```

#### IntelliJ Run Configuration

- Create Intellij Run Configuration of type `Application`
- Pass following system properties (in `VM options`):
  - `-Dapp.environment=<env-type>`
  - `-Dlogback.configurationFile=logback/logback-local.xml`
- Pass following program arguments: `run verticle.com.ascend.testlab.MainVerticle`
- Pass following environment variables (in `Environment variables`):
  ```shell
  ENV=<env-type>
  MYSQL_USER=<mysql-username> \
  MYSQL_PASSWORD=<mysql-password> \
  ```

## Code Formatting

- Run `mvn com.spotify.fmt:fmt-maven-plugin:format` to auto-format the code
