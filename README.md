# thread-transactional-facade
# 前言
* 在开启事务的情况下，多租户之间切换数据源无效，想了很多方案，最终想到了使用多线程去处理，
然后解决多线程之间的事务同步即可，参考了一些资料，再结合自己的需求，
写了这个thread-transactional-facade，已经上线使用，供大家参考。
* 欢迎进群讨论(聊技术，吐槽、吹牛逼都可以) qq: 192137266  

# 介绍
* 使用CountDownLatch实现多线程事务管理；
* 配置log4j2动态日志
* 多线程数据库读写问题：如果通过多线程同时操作update和inset，则会出现死锁问题，因为MySQL的事务隔离级别默认是REPEATABLE_READ，需要解决该问题可以把事务级别降低为READ_COMMITTED。但是引申的问题在于同一条数据的update可能先于insert执行，就会导致update的where主见不存在，从而update失败

感谢： https://github.com/hexiehome/thread-transactional.git