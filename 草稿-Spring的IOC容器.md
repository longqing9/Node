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

- BeanFactory接口定义了基本的IOC容器规范及获取Bean的基本方法；
- HierarchicalBeanFactory接口继承了BeanFactory接口的基本功能，增加了获取父接口的BeanFactory的接口功能，使BeanFactory具备了双薪IOC容器的管理功能；
- ConfigurableBeanFactory接口中主要定义了一些对BeanFactory的配置功能，例如设置双亲IOC容器，配置Bean的后置处理器等。
- 在ListableBeanFactory接口中，细化了许多BeanFactory的接口的定义，例如 getBeanDefinitionNames()接口方法；
- ApplicationContext接口，通过继承MessageSource、ResourceLoader、ApplicationEventPublisher接口，在BeanFactory简单IOC容器的基础上添加了许多高级容器的特性的支持。
- ListableBeanFactory和HierarchicalBeanFactory两个接口，连接BeanFactory接口定义和ApplicationContext应用上下文的接口定义。
- 常用的应用上下文基本上都是ConfigurableApplicationContext或者WebApplicationContext的实现。
- 对于ApplicationContext而言，为了在Web环境中使用它，还设计了WebApplicationContext接口，而这个接口通过继承ThemeSource接口来扩充功能。

**IOC容器接口设计的两条主线**：

1、从接口BeanFactory到HierarchicalBeanFactory，再到ConfigurableBeanFactory，是一条主要的BeanFactory设计路径；

2、以ApplicationContext应用上下文接口为核心的接口设计：从BeanFactory到ListableBeanFactory，再到ApplicationContext，再到WebApplicationContext或者ConfigurableApplicationContext接口。

##### 2.2、BeanFactory的应用

BeanFactory提供了最基本的IoC容器功能，定义了IoC容器的最基本形式，并且提供了IoC容器所应该最受的最基本的服务规范。在Spring的代码实现中，BeanFactory知识一个接口类，并没有给出具体的实现。但是常见的DefaultListableBeanFactory、XmlBeanFactory、ApplicationContext等都可以看成是容器附加了某些功能的具体实现，也就是容器体系中的具体容器实现。

BeanFactory接口设计了getBean方法，这个方法是使用IoC容器API主要方法，另外还有获取Bean的别名、获取Bean的类型、获取属性、作用域等方法。

**FactoryBean和BeanFactory的比较**：

一个是Factory，也就是IoC容器或对象工厂；一个Bean。在Spring中，所有的Bean都是由BeanFactory(也就是IoC容器)来进行管理。但是对FactoryBean而言，这个Bean不是简单的Bean，而是一个能产生或者修饰对象生成的工厂Bean，它的实现与设计模式中的工厂模式和修饰器模式类似。

##### 2.3、BeanFactory的原理

BeanFactory接口提供了使用IoC容器的规范，在这个基础上，Spring还提供了符合这个IoC容器接口的一系列容器的实现供开发人员使用。以XMLBeanFactory的实现为例：

![](E:\Node\picture\xmlBeanFactory.jpg)

XMLBeanFactory继承自DefaultListableBeanFactory类，在Spring中，实际上是把DefaultListableBeanFactory作为一个默认的功能完整的IoC容器来使用，XMLBeanFactory在通过继承拥有容器的功能同时，也增加了以XML文件方式定义的BeanDefinition的IoC容器。

##### 2.4、ApplicationContext的应用

ApplicationContext除了能够提供容器的简单功能外，还提供了以下服务：

- 支持不同的信息源：其继承了MessageContext接口，这些信息源的扩展功能支持国际化的实现。
- 访问资源：这一特性体现在ResourceLoader和Resource的支持上，这样可以从不同的地方获取Bean定义资源；
- 支持应用事件。继承了接口ApplicationEventPublisher，从而在上下文引入了事件机制。这些事件和Bean的生命周期的结合为Bean的管理提供了便利；

#### 3、IoC容器的初始化过程

IoC的初始化过程分为三个阶段，分别是：资源定位、Bean的载入和解析、Bean的注册；

- 资源定位：是对BeanDefinition的资源定位，由ResourceLoader通过统一的Resource完成；
- Bean的载入和解析：把定义的Bean标识为IoC容器的内部数据结构--BeanDefinition；
- Bean的注册：就是吧载入过程解析得到的BeanDefinition注册到IoC容器内，IoC内部将BeanDefinition添加到HashMap中；

完成了BeanDefinition的注册，就完成了IoC容器的初始化过程，此时，在使用IoC容器DefaultListableBeanFactory中一件建立整个Bean的配置信息，并且这些BeanDefinition已经可以被容器使用（可以再BeanDefinitionMap中被检索和使用），容器的作用就是对这些信息进行处理和维护。

#### 4、IoC容器的依赖注入

依赖注入的时机是用户第一次向IoC容器获取Bean时触发，懒加载除外（懒加载是在







#### 1、Spring IOC容器

Spring IOC容器负责创建对象，管理对象（通过依赖注入（DI）），装配对象，配置对象，并且管理这些对象的整个生命周期。IOC是Spring所倡导的开发方式，所有的类都会在Spring容器中注册。

**IOC的优点**：IOC 或依赖注入把应用的代码量降到最低。它使应用容易测试，单元测试不再需要单例和 JNDI 查找机制。最小的代价和最小的侵入性使松散耦合得以实现。IOC 容器支持加载服务时的饿汉式初始化和懒加载。

Spring的IOC容器































通常情况下，被注入对象会直接依赖于被依赖对象，但是，在IOC的场景中，两者之间通过IoC Service Provider打交道的，所有的被注入对象和依赖对象现在由IoC Service Provider统一管理。被注入对象需要什么直接跟IoC Service Provider招呼一声，后者就会把相应的被依赖对象注入到被注入对象中，从而达到IoC Service Provider为被注入对象服务的目的。IoC Service Provider在这里就是通常的IoC容器所充当的角色。从被注入对象的角度看，与之前直接寻求依赖对象相比，依赖对象的取得方式发生了反转，控制也从被注入对象转到了IoC Service Provider那里。