<h1 align = "center">SpringCloud</h1>

### 一、springCloud之Eureka

```text
eureka在新版中，在jar包中将eureka的客户端和eureka的服务端进行分离。在不同的eureka服务端和客户端引入不同的jar包。
```

1、微服务中使用Eureka的步骤如下：

- 引入jar包：

  ```java
  Eureka的客户端：
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
      </dependency>
  Eureka服务端：
      <dependency>
          <groupId>org.springframework.cloud</groupId>
          <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
      </dependency>
  ```

- 修改配置文件：

  ```yaml
  单机模式下-eureka服务端：
  eureka:
    instance:
      # 服务实例名称
      hostname: localhost
    client:
    	# 是否将本服务注册到eureka注册中心
      register-with-eureka: false
      # 是否要需要注册中心检索本服务 false表示自己端就是注册中心，维护服务实例，不需要去检索服务
      fetch-registry: false
      service-url:
        # 服务端 标注自己的对外访问的服务地址
        defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  ```

  ```yaml
  单机模式下-eureka客户端：
  spring:
    application:
      # 当前服务的实例名称
      name: cloud-consumer-apply
  eureka:
    client:
      # 是否要需要注册中心检索本服务 false表示自己端就是注册中心，维护服务实例，不需要去检索服务
      fetch-registry: true
      # 是否将本服务注册到eureka注册中心
      register-with-eureka: true
      service-url:
        # 客户端访问服务端的地址
        defaultZone: http://localhost:7001/eureka
  ```

- 修改业务启动类：

  ```java
  // 如果是在eureka的服务端添加如下注解
  @EnableEurekaServer
  // 如果实在eureka的客户端添加如下注解
  @EnableEurekaClient
  ```

  