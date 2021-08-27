一、RabbitMQ命令

```tex
1、MQ服务器的启动：rabbitmqctl start_app
2、MQ服务器状态：  rabbitmqctl status
3、创建用户：		 rabbitmqctl add_user [账号] [密码]
4、设置访问权限：   rabbitmqctl setpermissions -p / [账号]  ".*" ".*" ".*"
5、设置用户权限：	rabbitmqctl set_user_tage [administrator | ]
```

