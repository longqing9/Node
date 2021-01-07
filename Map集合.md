---
title: Map集合
date: 2019-06-12 13:24:02
tags: "Map"
categories: "java"
---

HashMap的数据结构为数组和链表组合构成的；

#### 1.1  Map存储过程

​    在Map中是采用key-value键值对的形式进行存储数据的，当put数据时，会首先对key进行hash计算，获取key在数组中的索引值，根据索引值存储到对应的数组元素中，如果多个不同的key经过hash计算之后获取到的索引值相同，会以链表的形式存储到对应的数组元素的后面。再Java8之前，链表在插入数据时在Java8之前采用的是头插发，Java8级以后采用的是尾插法。

#### 1.2 Map扩容机制

​     1.Map的默认存储的长度为16，默认的负载因子为0.75；Map的默认的长度使用16的原因是，在Map内进行计算key的索引值时是采用按位与记性计算，这种计算的方式比数值计算的效率要高出很多。同时在进行计算式采用长度-1和hashCode值进行计算，这样可以实现数据的均匀分布。

​    2.当Map中存储的数量超出（当前长度*负载因子）就会进行扩容。每次扩容的大小是当前容量的2倍。在创建Map集合时可以设置负载因子和初始容量的大小；当Map进行扩容时首先创建一个Entry空数组，大小是之前的2倍，然后进行遍历之前的数组，将原来的数组中存在的值进行一次新的Hash计算，重新将存储在新的数组中，如果存在相同索引值，尾插法存储在链表中。在java8及以后的版本，数组的链表存在**平衡树**的部分，这样大大减低了查询的时间复杂度。

​     3.在并发情况下，Map进行扩容时使用头插法，可能会出现循环链表，在查询时出现死循环。使用尾插法在同样的前提下就不会出现，原因是扩容转移后前后链表顺序不变，保持之前节点的引用关系。

#### 1.3 Map常用方法

- `int size();`：返回Map的key-value对的长度。

- `boolean isEmpty();`：判断该Map是否为空。

- `boolean containsKey(Object key);`：判断该Map中是否包含指定的key。

- `boolean containsValue(Object value);`：判断该Map是否包含一个或多个value。

- `V get(Object key);`：获取某个key所对应的value；若不包含该key，则返回null。

- `V put(K key, V value);`：向Map添加key-value对，当Map中有一个与该key相等的key-value对，则新的会去覆盖旧的。

- `V remove(Object key);`：移除指定的key所对应的key-value对，若成功删除，则返回移除的value值。

- `void putAll(Map m);`：将指定的Map中的key-value对全部复制到该Map中。

- `void clear();`：清除Map中的所有key-value对。

- `Set keySet();`：获取该Map中所有key组成的Set集合。

- `Collection values();`：获取该Map中所有value组成的Collection。

- `Set> entrySet();`：返回该Map中Entry类的Set集合。

- `boolean remove(Object key, Object value)`：删除指定的key-value对，若删除成功，则返回true；否则，返回false。

####  1.4 Map其他子类介绍

##### 1.4.1 HashTable

​	HashTable和HashMap都属于Map的子类，区别在于：

- HashTable是线程安全的，在其方法中使用了synchronized关键字进行修饰；
- 在HashTable中不允许key、Vale为null值，在HashMap允许为null；
- 由于在HashTable加锁，所以性能较低HashMap的性能高；

##### 1.4.2 TreeMap

​	TreeMap是SortedMap接口的实现类，其底层使用的数据结构为**红黑树**，key-value作为一个红黑树的节点，在进行数据存储是会进行根据key排序，排序的规则为自然排序和自定义排序。而HashMap和HashTable是无序的。



