用法：
1. 导包(写上父类来管理版本)
2. 写配置 详见 com.abdecd.tokenlogin.common.property.AllProperties
3. 加 @MapperScan("你的包名") 到主包的配置类中 (否则扫描路径会被覆盖)