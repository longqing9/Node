#### 1、Spring Bean的生命周期

在传统的Java应用中，Bean的生命周期很简单。使用Java关键字new进行Bean实例化，然后该对象就可以使用，一旦该对象不再被使用，则由Java自动进行垃圾回收。

相比之下，Spring容器中Bean的生命周期比较复杂。Spring容器中的Bean的创建到销毁要经历以下几步：Bean的实例化--》Bean属性注入--》Bean的初始化--》此时bean可以被正常使用--》Bean的销毁。如下图所示：

![](E:\Node\picture\spring-bean.jpg)

- Bean的实例化：Spring对Bean进行实例化；
- Bean属性注入：Spring将值和Bean的引用注入到Bean的属性；
- Bean的初始化：该过程比较复杂，会进行以下几种判断：
  - 1、如果Bean实现了BeanNameAware接口，Spring将Bean的ID传递给setBeanFactory()方法；
  - 2、如果Bean实现了BeanFactoryAware接口，Spring将调用setBeanFactory()将BeanFactory实例传入；
  - 3、如果实现了ApplicationContextAware接口，Spring将调用setApplicationContext()方法，将Bean所在的上下文引用传过来；
  - 4、如果Bean实现了BeanPostProcessor接口，Spring将调用postProcessorBeforeInitialization方法。
  - 5、如果Bean实现了InitializingBean接口，Spring将调用他们的afterPropertiesSet()方法，类似地，如果Bean使用init-menthod声明初始化方法，该方法也会被调用。
  - 6、如果Bean实现了BeanPostProcessor接口，Spring将调用它们的post-ProcessAfterInitialization()方法；
- 此时，Bean春被就绪，可以被应用程序调用，并且会被存在应用上下文中，直到该应用下上文被销毁；
- Bean的销毁，该过程会进行以下判断：
  - 如果Bean实现了DisposableBean接口，Spring将调用它的destroy()接口方法；
  - 如果Bean使用destroy-method声明了销毁方法，该方法也会被调用。

#### 2、Spring Bean的作用域

在默认情况下，Spring应用上下文中所有Bean都是作为单例的形式创建的，也就是说，不管给定的一个Bean被注入到其他Bean多少次，每次注入的都是同一个实例；

Spring定义了多种作用域，可以基于这些作用域创建Bean，包括：

- 单例(Singleton)：在整个应用中，只会被创建Bean的一个实例；
- 原型(prototype)：每次注入或者通过Spring应用上下文获取的时候，都会创建一个新的Bean实例；
- 会话(Session)：在web应用中，为每个会话创建一个Bean实例；
- 请求(Request)：在Web应用中，为每个请求创建一个Bean实例。

