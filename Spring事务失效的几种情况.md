---
title: Spring事务失效的几种情况
date: 2021-01-20 19:57:57
tags: [Spring,事务]
categories: Spring
---

#### 一、Spring事务的七种传播机制

- required：业务方法需要在一个容器里执行。如果方法运行时已经处于一个事务中，那么加入到这个事务。否则自己新建一个新的事务；
- surpported：该方法在某个事务范围内被调用，则方法成为该事务的一部分。如果方法在事务范围外被调用，该方法就在没有事务的环境下执行；
- requires-new：不管是否存在事务，该方法总会为自己发起一个新事务。如果方法已经运行在一个事务中，则将当前事务挂起，重新创建一个新事务；
- not-surpported：声明方法不需要事务。如果方法没有关联到一个事务，容器不会为他开启事务，如果方法在一个事务中被调用，该事务会被挂起，调用结束后，原先的事务会恢复执行；
- mandatory：该方法只能在一个已经存在的事务中执行，业务方法不能发起自己的事务。如果在没有事务的环境下被调用，容器抛出异常；
- nested：如果一个活动的事务存在，则运行在一个嵌套的事务中。如果没有活动事务，则按照required属性执行。（内部的回滚不会对外部产生影响，但是外部回滚时内部会一起回滚）；
- never：该方法绝对不能在事务范围内执行，如果在就抛出异常。只有该方法没有关联到任何事务，才能正常执行。

#### 二、Spring事务失效的几种情况

##### 1、数据库引擎

Mysql的MyISAM引擎不支持事务回滚，如果需要数据回滚，需要将Mysql的引擎设置为InnoDB;

##### 2、对异常进行捕获

Spring事务中默认的事务管理只能处理显示抛出的RuntimeException，如果对该异常进行try...catch处理，事务将不会生效；

例如：

```java
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean addProduct(Product product){
        int result = 0;
        try {
            result = productDao.insert(product);
            int q = 12 /0;
        }catch (Exception ex){
            ex.getStackTrace();
        }
        return result > 0;
    }
```

##### 3、方法不为公共方法

使用@Transaction注解，该注解只针对public方法生效。同样需要指定出现那种异常的清下进行回滚：@Transactional(rollbackFor = Exception.class);

例如：

```java
    @Transactional(rollbackFor = Exception.class)
    private Boolean updateProduct(Product oldProduct,Product newProduct){
        if (!Objects.isNull(newProduct.getAmount())){
            oldProduct.setAmount(newProduct.getAmount());
        }
        if (!Objects.isNull(newProduct.getName())){
            oldProduct.setName(newProduct.getName());
        }
        if (!Objects.isNull(newProduct.getVersion())){
            oldProduct.setVersion(newProduct.getVersion());
        }

        return productTestService.updatetest(oldProduct);
    }
```

##### 4、方法内部调用

同类中事务的方法不能嵌套在其他方法中，Q类中A方法调用B方法，B方法开启事务注解，B方法中事务不会生效。

例如：

```java
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    public Boolean updateProduct(Product product){
        updateProductInfo(product);
        return updateProduct(product1,product);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateProduct1(Product product){
        int result = productDao.updateById(product);
        return result > 0;
    }
}
```

##### 5、事务覆盖

当类配置了@Transactional，方法也配置了@Transactional，方法的事务会覆盖类的事务配置信息。

##### 6、事务传播行为设置错误

@Transactional 注解属性 propagation（事务的传播行为） 设置错误；

```java
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductDao productDao;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean outMethod(Product product){
        //添加数据
     	productDao.insert(product);
        product.setId(1352815665135575041L);
        // 更新数据
        return innerMethod(product);
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean innerMethod(Product product){
        int result = productDao.updateById(product);
        return result > 0;
    }
}
```

根据上述代码，说明@Transactional 注解属性 propagation，事务的传播机制如下：

| propagation属性                     | outMethod()无事务        | innerMethod                                        |
| ----------------------------------- | ------------------------ | -------------------------------------------------- |
| Propagation.MANDATORY               | 抛出异常                 | 在外部方法的事务中运行                             |
| Propagation.NEVER                   | 不在事务中运行           | 内部方法抛出异常                                   |
| Propagation.NOT_SUPPORTED           | 不再事务中运行           | 外部方法的事务暂停直至内部方法运行完成             |
| Propagation.REQUIRED ( **默认值** ) | 新开一个事务并在其中运行 | 在外部方法的事务中运行                             |
| Propagation.REQUIRES_NEW            | 新开一个事务并在其中运行 | 外部方法的事务暂停直至内部方法中新开事务并执行完毕 |
| Propagation.SUPPORTS                | 不再事务中运行           | 在外部方法中事务中运行                             |

