# QuJing(曲境)

曲境是一个xposed模块，可实现在PC浏览器上动态监控（hook）函数调用和查看堆栈信息，及反射调用（invoke）等功能。避免了频繁写hook代码的麻烦，提供了可视化的界面，对新手更友好。

## 实现功能

1. 枚举安卓设备所有APP
2. 根据类名和方法名搜索方法
3. 方法监控，打印调用栈和出入参
4. 对目标方法强制执行

## 已知问题

1. 函数频繁调用时，巨量的堆栈信息和出入参打印在浏览器页面，会导致页面内容过多，如无必要数据，可刷新解决。
2. 部分前端显示存在布局问题
3. 不支持开机瞬间hook的场景（因为每次开机后需要设置需要hook的APP）
4. 对于本身不具备网络权限的APP暂不支持
5. ~~对LSPOSED的支持好像还有问题，现象是不能打开61000端口，晚点支持~~（LSPOSED已经支持，使用时不仅仅要在Lsposed中勾选目标APP，**还需要勾选“系统框架”哦**[如图](https://github.com/Mocha-L/QuJing/assets/24688287/53906a4e-96ca-4824-83c5-eb5cc70dd2af)）

## 使用方法

可直接下载apk目录的apk文件安装体验，具体的使用方法可以看[这篇文章](https://mp.weixin.qq.com/s/zXRKNximCk5DDFfFZcxDJQ)。

如果帮到你，记得点个star哦。

## 效果展示

列举手机中的所有APP供选择

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E9%85%8D%E7%BD%AE%E7%9B%AE%E6%A0%87%E5%BA%94%E7%94%A8.png" width="1000px" />

对选中的APP指导手动操作

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%89%8B%E5%8A%A8%E6%93%8D%E4%BD%9C%E6%8C%87%E5%AF%BC.png" width="1000px" />

进入APP，搜索类和方法进行监控

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%90%9C%E7%B4%A2%E7%9B%AE%E6%A0%87%E7%B1%BB-%E6%96%B9%E6%B3%95.png" width="1000px" />

进入监控方法，查看基本信息和调用堆栈，打印出入参数。

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E7%9B%91%E6%8E%A7%E6%96%B9%E6%B3%95.png" width="1000px" />
<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E7%9B%91%E6%8E%A7%E6%96%B9%E6%B3%952.png" width="1000px" />

执行方法支持类型

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%89%A7%E8%A1%8C%E6%96%B9%E6%B3%95.png" width="1000px" />


## 问题交流

如有疑问可以提issue，也欢迎大家进一步优化和提交PR。

也欢迎进入我的知识星球“爬虫三十六计”。

<img src="https://github.com/Mocha-L/QuJing/blob/master/image/%E6%98%9F%E7%90%83.png" width="400px" />

## 鸣谢

项目依据[xserver](https://github.com/monkeylord/XServer)优化而来，感谢原作者。

感谢[@小黄鸭爱学习](https://github.com/HuRuWo)的帮助优化，解决部分情况下classloader错误的问题。
