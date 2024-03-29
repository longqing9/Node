---
title: MySQL的逻辑架构及索引优化
date: 2021-06-13 14:32:19
tags: [MySQL]
categories: "MySQL"
---

### 1、MySQL的逻辑架构

#### 1.1 MySQL的逻辑机构图

MySQL的架构可以在多种不同的场景中应用并发挥良好的作用，主要体现在存储引擎在架构上，插件式的存储引擎架构将查询处理和其他系统任务以及数据的存储提取相分离，这种架构可以根据业务的需求和实际需求选择合适的存储引擎；

![img](https://longqing9.gitee.io/blog/images/mysql0.png)

#### 1.2、数据库的各层及介绍：

1. **连接层**：最上层是一些客户端和连接服务，包含本地socket通信和大多数基于客户端/服务端工具实现类似tcp/ip的通信。主要完成一些类似于连接处理、授权认证、及相关的安全方案。在该层上引入线程池的概念，为通过认证安全接入的客户端提供线程，同样在该层上可以实现基于SSL的安全链接，服务器也会为安全接入的每一个客户端验证它所具有的操作权限；
2. **服务层**：第二层架构主要完成大多数的核心服务功能，如SQL接口，并完成缓存的查询，SQL的分析和优化及部分内置函数的执行。所有跨存储引擎的功能也在这一层实现，如过程、函数等。在该层，服务器会解析查询并创建相应的内部解析树，并对其完成相应的优化如何确定查询表的顺序，是否利用索引等，最后生成相应的执行操作，如果是select语句，服务器还会查询内部缓存，如果内部缓存空间足够大，这样在解决大量读操作的环境中能够很好的提升系统的性能。
3. **引擎层**：存储引擎，存储引擎真正的负责了MySQL中数据的存储和提取，服务器通过API与存储引擎进行通信。不同的存储引擎具有的功能不同，这样可以根据自己的实际需求进行选取对应的存储引擎，常见的存储引擎为：InnoDB和MyISAM；
4. **数据存储层**：数据存储层，主要将数据存储在运行于裸设备的文件系统之上，并完成与存储引擎的交互。

#### 1.3、存储引擎简介

##### 1.3.1 命令：

1. 查看mysql的存储引擎：show engines;

2. 查询当前存储引擎：show variables like '%storage_engine%';

##### 1.3.2 InnoDB和MyISAM的对比

| 对比项   | MyISAM                                                   | InNoDB                                                       |
| -------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| 主外键   | 不支持                                                   | 支持                                                         |
| 事务     | 不支持                                                   | 支持                                                         |
| 行表锁   | 表锁，即使操作一条记录也会锁住整个表，不适合高并发的操作 | 行锁，操作时只锁某一行，不对其他行有影响，适合高并发操作     |
| 缓存     | 只缓存索引，不缓存真实数据                               | 不仅缓存索引还要真实数据，对内存要求较高，而且内存大小性能有决定性的影响 |
| 表空间   | 小                                                       | 大                                                           |
| 关注点   | 性能                                                     | 事务                                                         |
| 默认安装 | Y                                                        | Y                                                            |

### 2、索引优化分析

#### 2.1、MySQL的语句的解析顺序：

![img](https://longqing9.gitee.io/blog/images/mysql1.png)

#### 2.2、常见的join查询：

![img](https://longqing9.gitee.io/blog/images/mysql2.png)

![img](https://longqing9.gitee.io/blog/images/mysql3.png)

![img](https://longqing9.gitee.io/blog/images/mysql4.png)

### 3、MySQL的索引

#### 3.1 索引介绍

- 索引是帮助MySQL高效获取数据的数据结构，其本质是数据结构，目的是在于提高查询效率；
- 索引可以简单的理解为排好序的快速查找数据结构；
- 一般来说索引本身也很大，不可能全部存储在内存中，因此索引往往以索引文件的形式存储在磁盘上；

#### 3.2 索引的优劣势

##### 3.2.1 优势

- 数据检索的效率，降低数据库的IO成本；
- 通过索引列对数据进行排序，降低数据排序的成本，降低了CPU的消耗；

##### 3.2.2 索引的劣势：

- 索引实际上也是一张表，该表保存了主键与索引字段，并指向实体表的记录，所以索引列也是要占用空间的；
- 虽然索引大大提高了查询速度，同时却会降低更新表的速度，在堆表进行增删改时，因为更新表时，MySQL不仅要保存数据还会保存索引文件每次更新添加了索引列的字段，都会调整因为更新所带来的的键值变化后的索引信息。
- 索引只是提高效率的一个因素，如果MySQL有大数据量的表，就需要花费时间研究建立最优秀的索引；

#### 3.3 索引的分类

- 单值索引：一个索引质保函单个列，一个表可以有多个单列索引；
- 唯一索引：索引列的值必须唯一，但允许为空；
- 复合索引：一个索引包含多个列；

#### 3.4 基本语法

- 创建索引：create [unique] INDEX INDEX_Name on tableName(columName(length));
- 添加索引：ALTER table ADD [unique] index [indexName] on (columName(length))；
- 删除索引：DROP index [indexName] on table;
- 查看索引：show index from table;

#### 3.5 索引分类

- 按数据结构分类可分为：**B+tree索引、Hash索引、Full-text索引**。
- 按物理存储分类可分为：**聚簇索引、二级索引（辅助索引）**。
- 按字段特性分类可分为：**主键索引、普通索引、前缀索引**。
- 按字段个数分类可分为：**单列索引、联合索引（复合索引、组合索引）**。

MySQL索引按数据结构分类可分为：**B+tree索引、Hash索引、Full-text索引**。

| -             | InnoDB         | MyISAM | Memory |
| ------------- | -------------- | ------ | ------ |
| B+tree索引    | √              | √      | √      |
| Hash索引      | ×              | ×      | √      |
| Full-text索引 | √（MySQL5.6+） | √      | ×      |

注：InnoDB实际上也支持Hash索引，但是InnoDB中Hash索引的创建由存储引擎引擎自动优化创建，不能人为干预是否为表创建Hash索引。

#### 3.6 需要创建索引的情况

- 主键自动创建唯一索引；
- 频繁作为查询条件的字段应该创建索引；
- 查询中与其他表关联的字段，外键关系建立索引；
- 频繁更新的字段不适合创建索引；
- where条件后用不到的字段不需要创建索引；
- 单键/组合索引的选择，在高并发下倾向创建组合索引；
- 查询中排序的字段，排序字段若通过索引区访问将大大提高排序速度；
- 查询中统计或分组的字段；

##### 3.7 不需要创建索引的情况

- 表记录太少；
- 经常增删改的表；
- 数据重复且分布平均的表字段，应该只为最经常查询和最经常排序的数据建立索引。注意：如果某个数据列包含许多重复的内容，为它建立索引没有太大的实际效果；