# dreamwork-base

#### 项目介绍
##base
提供一些常用的，独立的基础函数库，其中

###org.dreamwork.compilation 包
提供运行期动态编译的API，允许java代码在运行期产生动态产生java代码，并编译

###org.dreamwork.config 包
提供针对XML配置文件的解析API。
解析器将在运行期监视配置文件，若有改动，将刷新配置项。
XML配置文件的模式为：
根元素可以为任意名称，其中realtime属性标识了是否要实时监视文件修改。
<section>元素描述其name属性标识的元素将使用自定义的节点解析器来解析。节点解析器类名由parser属性决定。
在应用程序中，可以使用类似XPath的方式来索引配置文件中的配置项。如：
```
<application version=”1.0” runtime=”true”>
<module1>
    <item1>value1</item1>
</module1>
</application>
```
则可以用 module1.item1来索引，并返回 value1。

###org.dreamwork.fs 包
提供文件

#### 软件架构
软件架构说明


#### 安装教程

1. xxxx
2. xxxx
3. xxxx

#### 使用说明

1. xxxx
2. xxxx
3. xxxx

#### 参与贡献

1. Fork 本项目
2. 新建 Feat_xxx 分支
3. 提交代码
4. 新建 Pull Request


#### 码云特技

1. 使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2. 码云官方博客 [blog.gitee.com](https://blog.gitee.com)
3. 你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解码云上的优秀开源项目
4. [GVP](https://gitee.com/gvp) 全称是码云最有价值开源项目，是码云综合评定出的优秀开源项目
5. 码云官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6. 码云封面人物是一档用来展示码云会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)