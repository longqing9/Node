---
title: MySQL的索引
date: 2021-06-11 10:11:51
tags: [MySQL,索引]
categories: MySQL
---

索引(在MYSQL中也叫作“键(key)”)，是存储引擎用于快速查询记录的一种数据结构；索引的优化是对查询性能优化最有效的手段。索引可以包含一个或多个列的值，如果包含多个列，列的顺序十分重要，因为MYSQL只能高效的使用索引的最左前缀列。

### 1、索引的类型

索引有很多种类型，在MYSQL中，索引是在存储引擎层而不是服务器层实现的，因此，索引没有统一的索引标准：不同存储引擎的索引的工作方式并不一样，也不是所有的存储引擎都支持所有类型的索引；InnoDB存储引擎支持以下几种常见的索引：B+数索引、全文索引、哈希索引；

#### 1.1 B+Tree索引

B+树索引是目前关系型数据库中查找最常用和最为高效的索引，B+树索引的构造类似于二叉树，根据键值快速找到数据。B+树索引并不能找到一个给定键值的具体行，B+树索引能找到的只是被查找数据行所在的页，然后数据库通过把页读入到内存，再在内存中进行查找，最后得到要查找的数据。

**注意：** B+树并不是一个二叉树，而是最早是由平衡二叉树演化而来，B表示平衡（balance）。

B+树索引的**本质**是B+树在数据库中的实现，但是B+树在数据库中有一个特点就是高扇出性，因此在数据库中B+树的高度一般都是在2到4层。

**数据库那个B+树索引可以分为聚集索引和辅助索引**，但是不管是聚集索引还是辅助索引，其内部都是B+树，即高度平衡的，叶子结点存放着所有的数据，聚集索引与辅助索引不同的是，叶子结点存在的是否是一整行的信息。

##### 1.1.1 聚集索引

聚集索引就是按照每张表的主键构造一颗B+树，同时叶子结点中存放的即为整张表的行记录，也将聚集索引的叶子结点称为数据页。Innodb通过主键聚集数据，如果没有定义主键，innodb会选择非空的唯一索引代替。如果没有这样的索引，innodb会隐式的定义一个主键来作为聚簇索引。每个数据页也都是通过一个双项链表进行连接的。

由于实际的数据页只能按照一颗B+树进行排序，因此每张表只能拥有一个聚集索引，在多数情况下，索引查询优化器倾向于采用聚集索引，因为聚集索引能够在B+树索引的叶子节点上直接找到数据，此外，由于定义了数据的逻辑顺序，聚集索引能够特别快的访问针对范围值的查询，查询优化器能够快速找到某一段范围的数据页需要扫描。

聚集索引的存储并不是物理上连续的，而是逻辑上连续的。

- 数据页是通过双向链表链接，页按照主键的顺序排序
- 每个页中的记录也通过双向链表进行维护的，物理存储上可以同样不按照主键存储。

##### 1.1.2 辅助索引

辅助索引也称非聚集索引或二级索引，每个叶子节点并不包含行记录的全部数据。叶子结点除了包含键值意外，每个叶子结点中的索引行中含包含了一个相应行数据的聚集索引键。辅助索引的存在并不影响数据在聚集索引中的组织，因此每张表上可以有多个辅助索引。当通过辅助索引来寻找数据时，存储引擎会遍历辅助索引并通过页级别的指针获得指向主键索引的主键，然后再通过主键索引来找到一个完整的行记录。

#### 1.2 哈希索引

InnoDB存储引擎支持哈希索引是自适应的，InnoDB存储引擎会根据表的使用情况自动为表生成哈希索引，不能认为干预是否在一张表中生成哈希索引。

InnoDB存储引擎使用哈希算法对字典进行查找，其冲突机制采用链表方式，哈希函数采用除法散列方式，对于缓冲池页的哈希表来说，在缓冲池中的page页都有一个chain指针它指向相同哈希函数值的页。

Innodb存储引擎会监控对表上二级索引的查找，如果发现某二级索引被频繁访问，二级索引成为热数据，建立哈希索引可以带来速度的提升。经常访问的二级索引数据会自动被生成到hash索引里面去(最近连续被访问三次的数据)，自适应哈希索引通过缓冲池的B+树构造而来，因此建立的速度很快。

#### 1.3 全文检索

全文检索是将存储于数据库中的数据的任意内容信息查找出来的技术。

全文检索通常使用倒排索引来实现，倒排索引通B+树一样，也是一种索引结构，倒排索引需要将word存放到一张表中，这张表称为Auxiliary Table(辅助表)。它在辅助表中存储了单词与单词自身在一个或多个文档中所在位置之间的映射，通常是关联数组实现，拥有两种表现形式：

- inverted file index: 表现形式为{单词，单词所在文档的ID}
- full inverted index: 表现形式为{单词（单词所在的文档id，在具体文档中的位置）}；

InnoDB存储引擎中，为了提高全文检索的并行性能，共有6张辅助表，每张表根据word的Latin编码进行分区。另外InnoDB中采用全文检索索引缓存来提高全文检索的性能，全文检索索引缓存是一个红黑树结构。

##### 1.3.1 全文检索存在的限制

- 每张表只能有一个全文检索的索引；
- 由多列组合而成的全文检索的索引列必须使用相同的字符集与排序规则；
- 不支持没有单词界定符的语言，如中文、日语、韩语；



