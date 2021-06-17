---
title: 'SpringCloud'
date: 2020-12-21 17:15:08
tags: [Eureka,springcloud]
categories: "微服务"
---



### 一、springCloud之Eureka

```text
服务注册：Eureka 采用了CS的设计架构，Eureka Server作为服务器注册功能的服务器，他是服务的注册中心，而系统中的其他微服务，使用Eureka的客户端连接到Eureka Server并维持心跳连接，这样系统的维护人员就可以通过Eureka Server 来监控系统中各个微服务是否正常运行；
在服务注册于发现中，有一个服务注册中心，当服务启动的时候，会把当前自己服务器的信息，比如服务地址通讯地址等以别名的方式注册到注册中心上，另一方（服务的提供者），以该别名的方式去注册中心上获取实际的服务通讯地址，然后再实现本地RPC调用RPC远程调用。框架核心设计思想：在于注册中心，因为使用注册中心管理每个服务与服务之间的一个依赖关系（服务治理理念）。在任何rpc远程框架中，都会有一个注册中心（存放服务地址相关信息（地址信息））
```



```text
eureka在新版中，在jar包中将eureka的客户端和eureka的服务端进行分离。在不同的eureka服务端和客户端引入不同的jar包。
```

1、微服务中使用Eureka的步骤如下：

- 引入jar包：

  ```xml
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

  注意：在集群模式下，Eureka的服务端要将本台Eureka服务器分别注册到集群中的其他几台Eureka服务器上，地址使用逗号隔开。在客户端，要将本台客户端服务分别注册到集群中的每一台Eureka服务器中；

- 修改业务启动类：

  ```java
  // 如果是在eureka的服务端添加如下注解
  @EnableEurekaServer
  // 如果实在eureka的客户端添加如下注解
  @EnableEurekaClient
  ```

- Eureka实现负载均衡：

  在项目演示进行中，服务和服务之间的调用是通过RestTemplate 实现，在使用RestTemplate时，需要先创建配置类，使Spring容器能够加载到RestTemplate对象，配置类如下：

  ```java
  @Configuration
  public class ApplicationContextConfig {
      @Bean
      // 通过使用该注解 实现Eureka的调用的负载均衡的
      @LoadBalanced
      public RestTemplate getRestTemplate(){
          return new RestTemplate();
      }
  }
  ```

  在服务之间调用时。不在使用IP+端口的方式进行调用，而是使用在Eureka注册中心注册的服务实力的名称进行调用的，通过此方式在集群模式下实现多次请求时，同一个消费者可以多个服务的提供者之间调用，不会一直只由一个服务提供者提供服务。