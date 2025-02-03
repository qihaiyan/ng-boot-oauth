# ng-boot-oauth
oauth2 demo 程序，使用了angular 19 和 springboot框架。

默认的用户名是 `admin`, 密码是 `111111`
## 1. 特点
### 模块

项目包括3个模块：
* authserver
* ui (oauth2 code flow的客户端应用)
* ui-spa (oauth2 code flow with pkce 的客户端应用，有一个独立的前端模块)

### 前端
* Angular 19

### 后端
* Gradle 构建工具 
* Spring Boot
* Spring Authorization Server
* Thymeleaf 模版引擎

## 2. 在开发模式下运行
注意 **ui** 模块和 **ui-spa** 模块由于都用了8080端口，所以不能同时运行，每次只能运行一个模块。
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

### 运行 ui-spa 模块

* 运行后端服务
```bash
cd ui-spa
./gradlew bootRun
```

* 运行前端模块

如果是第一次运行程序，需要先安装依赖。
```bash
cd ui-spa
npm install
```

依赖安装完成后，运行前端程序
```bash
cd ui-spa
npm run dev
```

通过 `http://localhost:4200` 这个地址访问应用。
