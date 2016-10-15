# ng-boot-oauth
oauth2 demo 程序，使用了angularjs 1.x 和 springboot框架。

默认的用户名是 `admin`, 密码是 `111111`
## 1. 特点
### 模块

项目包括3个模块：
* authserver
* ui (oauth2 code flow的客户端应用)
* ui-implicit (oauth2 implicit flow的客户端应用，有一个独立的前端模块)

### 前端
* ES6
* AngularJs 1.x
* webpack dev server hot-reload
(通过在index.html中引用web-dev-server的js文件，在DEV模式下实现自动加载功能)
```
<% if (webpackConfig.metadata.ENV === 'dev') { %>
<!-- webpack dev server reload -->
<script src="http://<%= webpackConfig.metadata.host %>:<%= webpackConfig.metadata.port %>/webpack-dev-server.js"></script>
<!-- end webpack dev server reload -->
<% } %>
```

### 后端
* Gradle 构建工具 
* Spring Boot
* Spring security Oauth2
* Thymeleaf 模版引擎

## 2. 在开发模式下运行
注意 **ui** 模块和 **ui-implicit** 模块由于都用了8080端口，所以不能同时运行，每次只能运行一个模块。
### 获取代码
```bash
git clone https://github.com/qihaiyan/ng-boot-oauth.git
cd ng-boot-oauth
```

### 运行 OAuth2 Server
```bash
cd authserver
./gradlew bootRun
```

### 运行 ui 模块
```bash
cd ui
./gradlew bootRun
```
通过 `http://localhost:8080` 这个地址访问应用。

### 运行 ui-implicit 模块

* 运行后端服务
```bash
cd ui-implicit
./gradlew bootRun
```

* 运行前端模块

如果是第一次运行程序，需要先安装依赖。
```bash
cd ui-implicit
npm install
```

依赖安装完成后，运行前端程序
```bash
cd ui-implicit
npm run dev
```

* CORS 问题

在DEV模式下运行 ui-implicit 前端模块时，如果采用webpack-dev-server的默认配置会出现CORS错误。

需要在webpack.config.js中调整dev-server的配置，调用后端API的代理也可以在这儿进行配置：
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

通过 `http://localhost:8080` 这个地址访问应用。

## 3. 在生产模式下运行
编译和打包项目：
```bash
gradle build
```
运行 authserver:
```
cd authserver/
java -jar ./build/libs/ng-boot-oauth-0.0.1.jar
```
运行 ui 模块：
```
cd ui/
java -jar ./build/libs/ng-boot-oauth-ui-0.0.1.jar
```
或者是运行 ui-implicit 模块：
```
cd ui-implicit/
java -jar ./build/libs/ng-boot-oauth-ui-implicit-0.0.1.jar
```

通过 `http://localhost:8080` 这个地址访问应用。
