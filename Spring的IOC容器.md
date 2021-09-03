#### 1、IoC容器和IoC(依赖反转)模式

IoC模式：简单说就是依赖对象的获取方式由对象持有者创建变为由框架和IOC容器完成创建和属性的赋值。这种方式在解耦代码的同时提高了代码的可测试性。

IoC容器是实现IoC模式的载体，它可以在对象生产或初始化时直接将数据注入到对象中，也可以通过将对象的引用注入到对象数据域中的方式来注入对方法调用的依赖，这种依赖注入是可以递归的，对象被逐层注入。

在Spring中，Spring IoC提供了一个基本的JavaBean容器，通过IoC模式管理依赖关系，并通过依赖注入和AOP切面增强了为JavaBean这样的POJO对象赋予事务管理、生命周期管理等基本功能。从开发者的角度来看，IoC容器主动管理对象之间的依赖关系，并将依赖关系注入到组建中，使得对象间的依赖关系和管理变得更加灵活。

#### 2、IoC容器的实现

在Spring IoC容器的具体实现中主要分为两类：

- 实现BeanFactory接口的简单容器：该类容器只实现了容器的基本功能；
- ApplicationContext应用上下文：作为容器的高级形态而存在，该类容器在简单容器的基础上增加了很多框架的特性，同时对应用环境做了很多适配。

在Spring提供的基本IoC容器的接口定义和实现基础上，Spring通过定义BeanDefinition来管理基于Spring的应用中的各种对象及对象间的依赖关系。BeanDefinition抽象了对Bean的定义，是让容器作用的主要数据结构。对于IoC容器来说，BeanDefinition就是对依赖反转模式中管理的对象依赖关系的数据抽象，也是容器实现依赖反转功能的核心数据结构，依赖反转功能都是围绕对BeanDefinition的处理来实现的。

##### 2.1、IoC容器的接口设计

<img src="E:\Node\picture\ioc-interface.jpg" />

- BeanFactory接口定义了基本的IoC容器规范及获取Bean的基本方法；
- HierarchicalBeanFactory接口继承了BeanFactory接口的基本功能，增加了获取父接口的BeanFactory的接口功能，使BeanFactory具备了双亲IoC容器的管理功能；
- ConfigurableBeanFactory接口中主要定义了一些对BeanFactory的配置功能，例如设置双亲IoC容器，配置Bean的后置处理器等。
- 在ListableBeanFactory接口中，细化了许多BeanFactory的接口的定义，例如 getBeanDefinitionNames()接口方法；
- ApplicationContext接口，通过继承MessageSource、ResourceLoader、ApplicationEventPublisher接口，在BeanFactory简单IoC容器的基础上添加了许多高级容器的特性的支持。
- ListableBeanFactory和HierarchicalBeanFactory两个接口，连接BeanFactory接口定义和ApplicationContext应用上下文的接口定义。
- 常用的应用上下文基本上都是ConfigurableApplicationContext或者WebApplicationContext的实现。
- 对于ApplicationContext而言，为了在Web环境中使用它，还设计了WebApplicationContext接口，而这个接口通过继承ThemeSource接口来扩充功能。

**IOC容器接口设计的两条主线**：

1、从接口BeanFactory到HierarchicalBeanFactory，再到ConfigurableBeanFactory，是一条主要的BeanFactory设计路径；

2、以ApplicationContext应用上下文接口为核心的接口设计：从BeanFactory到ListableBeanFactory，再到ApplicationContext，再到WebApplicationContext或者ConfigurableApplicationContext接口。

##### 2.2、BeanFactory的应用

BeanFactory提供了最基本的IoC容器功能，定义了IoC容器的最基本形式，并且提供了IoC容器最基本的服务规范。在Spring的代码实现中，BeanFactory只是一个接口类，并没有给出具体的实现。但是常见的DefaultListableBeanFactory、XmlBeanFactory、ApplicationContext等都可以看成是容器附加了某些功能的具体实现，也就是容器体系中的具体容器实现。

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
- Bean的注册：就是把载入过程解析得到的BeanDefinition注册到IoC容器内，IoC内部将BeanDefinition添加到HashMap中；

完成了BeanDefinition的注册，就完成了IoC容器的初始化过程，此时，在使用IoC容器DefaultListableBeanFactory中一件建立整个Bean的配置信息，并且这些BeanDefinition已经可以被容器使用（可以在BeanDefinitionMap中被检索和使用），容器的作用就是对这些信息进行处理和维护。

#### 4、IoC容器的依赖注入

依赖注入的时机默认是用户第一次向IoC容器获取Bean时触发；如果Bean的属性中设置了lazy-init属性，在对应BeanDefinition信息中通过控制lazy-init属性让容器完成对bean的预实例化，其本质上也是一个完成依赖注入的过程，但是它是在初始化的过程中完成的。

##### 4.1、IoC依赖注入的方式

**显示注入：**

- 构造方法注入：就是被注入对象可以通过其构造方法中生命的依赖对象的参数列表，让外部（通常是IoC容器）知道它需要依赖的对象；
- setter方法注入：通过setter方法可以更改响应的对象属性，通过getter方法，可以获得响应属性的状态。
- 接口注入：就是实现某个接口，在接口中提供一个方法，用来为其注入依赖对象；

**隐式注入：**

- IoC容器的自动装配：使用autowiring

##### 4.2、lazy-init和预实例化

IoC容器的初始化过程中，主要的工作是对BeanDefinition的定位、载入、解析和注册。此时依赖注入并没有发生，依赖注入的时机是应用第一次向容器索要Bean时，调用getBean来完成的。对于容器的初始化有一种情况是例外的，就是用户通过设置Bean的lazy-init属性来控制预实例化的过程，这个预实例化在初始化容器时完成Bean的依赖注入。

这种方式会对容器初始化的性能有一些影响，但却能够提高应用第一次获取Bean的性能，因为应用在第一次取得Bean时，依赖注入已经结束，应用可以取得已有的Bean。

##### 4.3、autowiring(自动依赖装配)的实现

IoC容器还提供了自动依赖装配方式，在自动装配中，不需要对Bean属性做显示的依赖关系声明，只需要配置好autowiring属性，IoC容器会根据这个属性的配置，使用**反射**自动查找数据的类型或名字，然后基于属性的类型或名字来自动匹配IoC容器中的Bean，从而自动地完成依赖注入。

对autowiring属性进行处理，从而完成对Bean属性的自动依赖装配是在populataBean中实现的。在populateBean的实现中，在处理一般的Bean之前，先对autowiring属性进行处理，如果当前Bean配置了autowiring_by_name和autowiring_by_type属性，name调用响应的autowiringByName方法和AutowiringByType方法。

对于autowiringByName方法，它首先通过反射机制从当前Bean中得到需要注入的属性名，然后使用这个属性名向容器中申请与之同名的Bean。这样实际又触发了另一个Bean的生成和依赖注入的过程。

##### 4.4、Spring IoC 如何解决循环依赖的问题

循环依赖的类型，根据注入的时机可以分为两种：

- 构造器循环依赖：依赖对象是通过构造方法传入，在实例化Bean的时候发生，构造器循环依赖，本质上是无解的，因此Spring不支持构造器循环依赖。
- 赋值属性循环依赖：依赖对象是通过setter方法传入的，对象已经实例化，在属性赋值和依赖注入时发生的。赋值属性循环依赖，Spring只支持Bean在单例模式下的循环依赖；

Spring为了解决单例的循环依赖问题，使用引入了三级缓存：

- **一级缓存：**singletonObjects：完成初始化的单例对象的缓存；
- **二级缓存：**earlySingletonObjects ：完成实例化但未初始化的提前暴露的单例对象的缓存；
- **三级缓存：**singletonFactories ：进入实例化阶段的单例对象工厂的缓存；

单利模式下的setter方法赋值循环依赖，其实现主要就是靠提前暴露创建中的单例实例：

<img src="E:\Node\picture\circular_dependency.jpg" style="zoom:80%;" />

- 创建A，调用构造方法完成构造，进行属性赋值注入，发现依赖B，去实例化B；
- 创建B，调用构造方法完成构造，进行属性赋值注入，发现依赖C，去实例化C；
- 创建C，调用构造方法完成构造，进行属性赋值注入，发现依赖A，A已经通过构造函数完成实例化，使用A的引用完成C的初始化；
- C完成初始化，注入B，B完成初始化，然后B注入A，A完成实例化。

从Spring 三级缓存中获取Bean的过程是，先从一级缓存中尝试获取需要的Bean，如果获取不到，则尝试从二级缓存中获取Bean的实例，最后尝试从三级缓存的对象工厂中获取Bean。