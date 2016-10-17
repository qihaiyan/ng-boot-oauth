# ng-boot-oauth
An oauth2 demo with angularjs and springboot.

Default username is `admin`, and password is `111111`.
## 1. Features
### Modules

The project contains 3 modules
* authserver
* ui (A client using oauth2 code flow)
* ui-implicit (A client using oauth2 implicit flow, with a standalone frontend module)

### Frontend
* ES6
* AngularJs 1.x
* webpack dev server reload
(by inserting a web-dev-server script into index.html when the environment is set to DEV )
```
<% if (webpackConfig.metadata.ENV === 'dev') { %>
<!-- webpack dev server reload -->
<script src="http://<%= webpackConfig.metadata.host %>:<%= webpackConfig.metadata.port %>/webpack-dev-server.js"></script>
<!-- end webpack dev server reload -->
<% } %>
```

### Backend
* Gradle Build Tool 
* Spring Boot
* Spring security Oauth2 integration
* Thymeleaf server-side Java template engine

## 2. RUNNING IN DEVELOPMENT MODE
Note that **ui** module and **ui-implicit** module can't be running at the same time, because they use the same port 8080.
### GET THE CODE
```bash
git clone https://github.com/qihaiyan/ng-boot-oauth.git
cd ng-boot-oauth
```

### RUNNING OAuth2 Server
```bash
cd authserver
./gradlew bootRun
```

### RUNNING ui MODULE
```bash
cd ui
./gradlew bootRun
```
Now we can visit the app at `http://localhost:8080`

### RUNNING ui-implicit MODULE

* RUNNING BACKEND SERVER
```bash
cd ui-implicit
./gradlew bootRun
```

If it's the first time to run ui-implicit module, install dependencies at first.
```bash
cd ui-implicit
npm install
```

* RUNNING DEV SERVER
```bash
cd ui-implicit
npm run dev
```

* CORS

We use the following configuration for dev-server in webpack.config.js to avoid the CORS error. And the proxies for api calls can also be configured here.
```
devServer: {
            port: 3000,
            contentBase: './src/main/frontend',
            headers: {
                'Access-Control-Allow-Origin': 'http://localhost:8080',
                'Access-Control-Allow-Credentials': 'true',
                'Access-Control-Allow-Headers': 'authorization',
                'Access-Control-Allow-Methods': 'GET'
            },
            // proxy: {
            //     '/user': 'http://localhost:8080/'
            // },
        },
```

Now we can visit the app at `http://localhost:8080`

## 3. RUNNING IN PRODUCTION MODE
Compile and package project:
```bash
gradle build
```
Then run auth server:
```
cd authserver/
java -jar ./build/libs/ng-boot-oauth-0.0.1.jar
```
Then run ui server:
```
cd ui/
java -jar ./build/libs/ng-boot-oauth-ui-0.0.1.jar
```
Or run ui-implicit server:
```
cd ui-implicit/
java -jar ./build/libs/ng-boot-oauth-ui-implicit-0.0.1.jar
```

Now we can visit the app at `http://localhost:8080`
