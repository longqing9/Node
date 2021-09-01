#### 1、IOC容器和IOC(依赖反转)模式

IOC模式：简单说就是依赖对象的获取方式由对象持有者创建变为由框架和IOC容器完成创建和属性的赋值。这种方式在解耦代码的同时提高了代码的可测试性。

IOC容器是实现IOC模式的载体，它可以在对象生产或初始化时直接将数据注入到对象中，也可以通过将对象的引用注入到对象数据域中的方式来注入对方法调用的依赖，这种依赖注入是可以递归的，对象被逐层注入。

在Spring中，Spring IOC提供了一个基本的JavaBean容器，通过IOC模式管理依赖关系，并通过依赖注入和AOP切面增强了为JavaBean这样的POJO对象赋予事务管理、生命周期管理等基本功能。从开发者的角度来看，IOC容器主动管理对象之间的依赖关系，并将依赖关系注入到组建中，使得对象间的依赖关系和管理变得更加灵活。

#### 2、IOC容器的实现

在Spring IOC容器的具体实现中主要分为两类：

- 实现BeanFactory接口的简单容器：该类容器只实现了容器的基本功能；
- ApplicationContext应用上下文：作为容器的高级形态而存在，该类容器在简单容器的基础上增加了很多框架的特性，同时对应用韩静做了很多适配。

在Spring提供的基本IoC容器的接口定义和实现基础上，Spring通过定义BeanDefinition来管理基于Spring的应用中的各种对象及对象间的依赖关系。BeanDefinition抽象了Java语言对Bean的定义，是让容器作用的主要数据结构。对于IoC容器来说，BeanDefinition就是对依赖反转模式中管理的对象依赖关系的数据抽象，也是容器实现依赖反转功能的核心数据结构，依赖反转功能都是围绕对BeanDefinition的处理来实现的。

##### 2.1、IOC容器的接口设计

<img src="E:\Node\picture\ioc-interface.jpg" />

IOC容器接口设计的几条主线：

1、从接口BeanFactory到HierarchicalBeanFactory，再到ConfigurableBeanFactory，是一条主要的BeanFactory设计路径；

- BeanFactory接口定义了基本的IOC容器规范及获取Bean的基本方法；
- HierarchicalBeanFactory接口继承了BeanFactory接口的基本功能，增加了获取父接口的BeanFactory的接口功能，使BeanFactory具备了双薪IOC容器的管理功能；
- ConfigurableBeanFactory接口中主要定义了一些对BeanFactory的配置功能，例如设置双亲IOC容器，配置Bean的后置处理器等。

2、以ApplicationContext应用上下文接口为核心的接口设计：从BeanFactory到ListableBeanFactory，再到ApplicationContext，再到WebApplicationContext或者ConfigurableApplicationContext接口。

- 常用的应用上下文基本上都是ConfigurableApplicationContext或者WebApplicationContext的实现。
- 在这个接口体系中，ListableBeanFactory和HierarchicalBeanFactory两个接口，连接BeanFactory接口定义和ApplicationContext应用上下文的接口定义。
- 在ListableBeanFactory接口中，细化了许多BeanFactory的接口的定义，例如 getBeanDefinitionNames()接口方法；
- 对于ApplicationContext接口，通过继承MessageSource、ResourceLoader、ApplicationEventPublisher接口，在BeanFactory简单IOC容器的基础上添加了许多高级容器的特性的支持。

3、具体的IoC容器都是在这个接口体系下实现的，例如：DefaultListAbleBeanFactory，这个基本的IoC容器的实现就是实现了ConfigurableBeanFactory，从而成为一个简单的IoC容器实现。其他的IoC容器，XMLBeanFactory都是在DefaultListAbleBeanFactory的基础上做的扩展；

4、这个接口体系是以BeanFactory和ApplicationContext为核心的；而BeanFactory又是IoC容器的最基本的接口，在ApplicationContext的设计中，一方面它继承了BeanFactory接口体系中的ListableBeanFactory，AutowireCapableBeanFactory、HierarchicalBeanFactory等BeanFactory的接口，具备了BeanFactory IoC容器的基本功能；另一方面，通过继承MessageSource、ResourceLoader、ApplicationEventPublisher接口，BeanFactory为ApplicationContext赋予了更高级的IoC容器特性。对于ApplicationContext而言，为了在Web环境中使用它，还设计了WebApplicationContext接口，而这个接口通过继承ThemeSource接口来扩充功能。





































#### 1、Spring IOC容器

Spring IOC容器负责创建对象，管理对象（通过依赖注入（DI）），装配对象，配置对象，并且管理这些对象的整个生命周期。IOC是Spring所倡导的开发方式，所有的类都会在Spring容器中注册。

**IOC的优点**：IOC 或依赖注入把应用的代码量降到最低。它使应用容易测试，单元测试不再需要单例和 JNDI 查找机制。最小的代价和最小的侵入性使松散耦合得以实现。IOC 容器支持加载服务时的饿汉式初始化和懒加载。

Spring的IOC容器































通常情况下，被注入对象会直接依赖于被依赖对象，但是，在IOC的场景中，两者之间通过IoC Service Provider打交道的，所有的被注入对象和依赖对象现在由IoC Service Provider统一管理。被注入对象需要什么直接跟IoC Service Provider招呼一声，后者就会把相应的被依赖对象注入到被注入对象中，从而达到IoC Service Provider为被注入对象服务的目的。IoC Service Provider在这里就是通常的IoC容器所充当的角色。从被注入对象的角度看，与之前直接寻求依赖对象相比，依赖对象的取得方式发生了反转，控制也从被注入对象转到了IoC Service Provider那里。