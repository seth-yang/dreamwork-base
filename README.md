# dreamwork-base

#### 项目介绍
##base
提供一些常用的，独立的基础函数库，其中

###- org.dreamwork.compilation 包
提供运行期动态编译的API，允许java代码在运行期产生动态产生java代码，并编译

###- org.dreamwork.config 包
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

###- org.dreamwork.fs 包
提供文件系统遍历工具包。
递归遍历指定的目录，并创建文件索引；针对于每个文件和目录，将调用注册的 IFileHandler 处理器来处理。
在遍历过程中，若目录发生变化，则仅对变化的文件或目录进行回调处理。

###- org.dreamwork.gson 包
主要是一些针对dreamwork内部集合类开发的gson的插件

###- org.dreamwork.i18n 包
国际化语言环境支撑。
通过适配器模式，屏蔽底层真正的资源绑定器之间的区别。
默认实现了 JdkResourceAdapter 和 XMLResourceAdapter

###- org.dreamwork.misc 包
提供了杂项工具，包括：
Base64算法的java实现
Zip算法压缩后进行Base64编码（及逆向过程）的工具类
IP的3种表达方式之间的转换工具类
关于MimeType的工具类

###- org.dreamwork.telnet 包
提供了基于
RFC 318 (TELNET), 
RFC 854 – RFC 861 (TELNET sub-options)
RFC 1073 (TELNET Window Size)
的TELNET协议栈的java实现

###- org.dreamwork.text 包
提供了部分文本解析操作的基础API，如HTML

###- org.dreamwork.util 包
提供一些集合接口及实现类
提供各种基于java.util. Comparable接口的排序算法
提供一些基础的类型转换的工具类
提供一些便捷的文件路径操作工具类