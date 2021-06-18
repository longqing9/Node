---
title: Map集合
date: 2019-06-12 13:24:02
tags: [集合,java]
categories: "java"
---

### 1、Map的实现类如下图所示：

![](E:\Node\picture\map.png)

### 2、HashMap说明

#### 2.1 HashMap原理

HashMap的结构如下：

![](E:\Node\picture\Map1.jpg)

HashMap中是采用key-value键值对的形式进行存储数据的，底层数据结构是由数组＋链表构成的。在JDK7中叫Entry，在JDK8中叫Node。

HashMap对象创建时，初始化长度为16（一个长度为16的Node数组），在进行put数据时根据key的hash值去计算一个index值（在数组中的位置）。如果两个不同的key进行hash计算获取的index值相同时，就会形成链表。index的计算方法： index = HashCode（Key） & （Length - 1）。

节点在插入链表时，java8之前采用的是头插法，在Java8之后采用尾插法。在HashMap扩容resize时，采用头插法会出现环形链表的情况。

为了提升查询效率，当链表中到达一定条就树化，当链表的长度>=8且数组长度>=64时，会把链表转化成红黑树；当链表长度>=8，但数组长度<64时，会优先进行扩容，而不是转化成红黑树；该链表就会转化为红黑树；当红黑树节点数<=6，自动转化成链表；在进行remove操作时，如果红黑数中左子树或右子树中的少量不超过1，也会转化为链表。

#### 2.2 HashMap的扩容机制

默认创建HashMap时，HashMap中数组的初始长度是16，负载因子为0.75f。创建HashMap可以初始化数组的长度和负载因子大小。

当HashMap进行put数据时，会判断当前集合中存储的元素个数，当HashMap中元素的个数超过（数组的长度 * 负载因子）时，HashMap开始进行扩容，在扩容时，HashMap会首先创建一个数组长度是为 （当前数组长度 * 2）的新数组，并开始遍历原HashMap，对现有的Node<Key,value>重新进行hash计算获取index值，存入新的Node[]数组中，如果存在相同索引值，尾插法存储在链表中。

#### 2.3 补充说明

##### 1、数组使用2倍扩容的原因

为了性能方面考虑，因为HashMap在计算Node的index值时，是通过位运算(hash & (length-1))实现的，这种实现方式的效率可以比 hash % （length -1）的计算方式高出10倍左右。

##### 2、在数组长度超出64的条件下，链表长度超出8开始树化的原因

根据泊松分布，在负载因为为0.75f时，单个hash槽内的数据长度为8的概率小于百万分之一；

##### 3、HashMap线程不安全的原因

因为HashMap的put方法和resize等操作都不是同步的。假设两个线程同一时间做put操作，就会有可能出现计算的size不正确。

##### 4、HashMap的Key为null

在HashMap中，允许存在一个Key为null的node。

##### 5、通过key获取元素是如何进行的

通过key获取元素时，先通过hash & （length- 1）获取元素在数组中的位置，然后再比较数组中的值，如果是链表则进行循环遍历比较，找到后返回数据。比较时，对于值对象，==比较的是两个对象的值，对于引用对象，比较的是两个对象的地址。

### 3、ConcurrentHashMap

ConcurrentHashMap底层是基于数组+链表组成的，使用volatile关键字修饰它的value和下一个节点，以此保证数据的可见性和有序性，使用CAS和Synchronized保证并发安全性；

#### 3.1 ConcurrentHashMap原理

ConcurrentHashMap的底层是数组和链表，其中并发问题是通过CAS和Synchronized解决的。ConcurrentHashMap的默认初始化大小是16，负载因子是0.75f，Hash冲突、树化的条件和HashMap的方式相同，ConcurrentHash中不允许key或value为null,在put时如果任意一个为null就会抛出空指针异常，ConcurrentHashMap在创建对象时，不会进行初始化，只有当第一次put时，才会进行初始化，初始化数组的默认空间大小为16，扩容时大小为2的幂次方。

#### 3.2 ConcurrentHashMap的get方法的过程

首先计算hash值，定位到该table索引位置，如果是首节点符合就返回

如果遇到扩容的时候，就会在新的数组上查找该节点，匹配就返回

以上都不符合的话，就往下遍历节点，匹配就返回，否则最后就返回null

#### 3.3 扩容过程

1、先创建一个新的数组，长度树原数组的2倍。

2、首先需要把老数组的值拷贝(通过Hash计算位置)到新数组上，数组拷贝是从数组末端开始的，设值时是通过CAS（putObjectVolatile）方法保证数据安全性。

3、在复制时，会先在原数组槽点上加锁，保证原数组槽点不能操作，原数组槽点上的节点设置到新数组之后会将原数组槽点设置为转转移节点，转移节点不可以新增数据。

4、如果有新的数据进行put的位置为转移节点，会等待新数组赋值完成，在进行put。

5、新数组赋值完成，会直接把新数组赋值给数组容器，至此扩容完成。

#### 3.4 补充说明

##### 1、concurrentHashMap是如何保证线程安全的

数组用volatile修饰主要是保证在数组扩容的时候保证可见性，使用CAS和Synchronized保证并发安全性。同时采用锁升级的优化方式，优先使用偏向锁优先同一线程然后再次获取锁，如果失败就升级为CAS轻量级锁，如果失败就自选，防止线程被系统挂起，最后如果以上都失败就升级为重量级锁，以此保证其高效率。

##### 2、ConcurrentHashMap不支持key或value为空的原因

concurrentHashMap可以保证线程安全，如果ConcurrentHashMap中的value为null，就无法判断value为空，还是没有对应的key。

##### 3、ConcurrentHashMap的效率问题

在线程安全的几个map实现类中，concurrentHashMap的效率是最高的，因为在它的内部的锁粒度更低，采用CAS和Synchronized实现线程安全的。HashTable则是直接使用Synchronized在数组上加锁保证线程安全的。

### 4、HashTable



### 3、TreeMap











#### 1.1  Map存储过程

在Map中是采用key-value键值对的形式进行存储数据的、底层数据结构是由数组＋链表构成的，在多线程环境下存在线程并发问题。HashMap数组的长度默认为16，加载因子默认为0.75f，当进行put数据时，会对key进行hash计算，获取到该key在数组中存在的位置，当出现两个key的hash值相同时，会将该数据以链表的形式插入到后面；当HashMap中的数据个数超过（当前数组长度 * 加载因子）时，HashMap内开始进行扩容；当某一个链表的长度到达8时，链表开始转化为红黑树。

#### 1.2 Map扩容机制

​     1.Map的默认存储的长度为16，默认的负载因子为0.75；Map的默认的长度使用16的原因是，在Map内进行计算key的索引值时是采用按位与进行计算，这种计算的方式比数值计算的效率要高出很多。同时在进行计算式采用长度-1和hashCode值进行计算，这样可以实现数据的均匀分布。

​    2.当Map中存储的数量超出（当前长度*负载因子）就会进行扩容。每次扩容的大小是当前容量的2倍。在创建Map集合时可以设置负载因子和初始容量的大小；当Map进行扩容时首先创建一个Entry空数组，大小是之前的2倍，然后进行遍历之前的数组，将原来的数组中存在的值进行一次新的Hash计算，重新将存储在新的数组中，如果存在相同索引值，尾插法存储在链表中。

​     3.在并发情况下，Map进行扩容时使用头插法，可能会出现循环链表，在查询时出现死循环。使用尾插法在同样的前提下就不会出现，原因是扩容转移后前后链表顺序不变，保持之前节点的引用关系。

​	 4.在JDK8中当链表的长度到达8时，链表开始转化为红黑树，当转化为红黑树后长度减少到6时，红黑树就会重新转化为链表；根据泊松分布，在负载因为为0.75f时，单个hash槽内的数据长度为8的概率小于百万分之一；

#### 1.3 线程安全的Map

HashMap在多线程环境下存在线程安全问题，可以使用Collections.synchronizedMap()或HashTable()或ConcurrentHashMap()在多线程环境下来代替HashMap();

##### 1.3.1 Collections.synchronizedMap()

Collections.synchronizedMap()其内部维护了一个普通的对象和一个排斥锁，在操作Map对象时，就会对方法上锁；

##### 1.3.2 ConcurrentHashMap

ConcurrentHashMap底层是基于数组+链表组成的，使用volatile关键字修饰它的value和下一个节点，以此保证数据的可见性和有序性，使用CAS和Synchronized保证并发安全性；如果链表的长度大于8也会转化为红黑树；

ConcurrentHashMap在进行put操作时，步骤如下：

- 根据可以计算出HashCode；
- 判断是否需要进行初始化；
- 根据当前key定位除Node，如果空则可以写入数据，利用CAS尝试写入，失败则进行自选保证成功；
- 如果当前位置的HashCode==moved == -1 ，则需要进行进行扩容；
- 如果都不满足则利用Synchronized锁写入数据；
- 如果数量大于8则进行转化为红黑树；

ConcurrentHashMap中采用锁升级的优化方式，优先使用偏向锁优先同一线程然后再次获取锁，如果失败就升级为CAS轻量级锁，如果失败就自选，防止线程被系统挂起，最后如果以上都失败就升级为重量级锁；

####  1.4 Map其他子类介绍

##### 1.4.1 HashTable

​	HashTable和HashMap都属于Map的子类，区别在于：

- HashTable不允许键或值为null，HashMap的键或值都可以为null。
- 实现方式不同，HashTable继承了DicTionary类，HashMap继承AbstractMap类；
- 初始化容量不同：HashMap的初始容量为16，HashTable初始容量为11，负载因子相同；
- 扩容机制不同：现有数量 > 总容量 X 负载因子时，HashMap扩容规则为当前容量翻倍，HashTable扩容规则为当前容量翻倍+1；
- 迭代器不同：HashMap中的Iterator迭代器是fail-fast的，HashTable的Enumerator不是fail-fast的。

##### 1.4.2 TreeMap

​	TreeMap是SortedMap接口的实现类，其底层使用的数据结构为**红黑树**，key-value作为一个红黑树的节点，在进行数据存储是会进行根据key排序，排序的规则为自然排序和自定义排序。而HashMap和HashTable是无序的。

#### 1.5 Map常用方法

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





