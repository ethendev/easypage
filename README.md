## [English](README-EN.md)

## easypage
非常简洁的mybatis分页插件，支持MySQL，Oracle数据库。  

## 使用教程

### 一、pom.xml中添加依赖

```
<dependency>
   <groupId>com.github.ethendev</groupId>
   <artifactId>easypage</artifactId>
   <version>1.0.1</version>
 </dependency>
```

### 二、配置plugins
如果是SpringMVC项目, 在mybatis-config.xml中添加如下代码：
```
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "mybatis-3-config.dtd">
<configuration>

  <settings>
      <setting name="logImpl" value="SLF4J"/>
  </settings>
  <!-- mybatis-config.xml -->
  <plugins>
      <plugin interceptor="com.github.ethendev.PageInterceptor" />
  </plugins>

</configuration>
```

如果是Spring Boot项目，在SqlSessionFactory的配置中添加plugins

````
@Bean(name = "sqlSessionFactory")
public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
    SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
    bean.setDataSource(dataSource);

    //添加分页插件
    PageInterceptor easypage = new PageInterceptor();
    bean.setPlugins(new Interceptor[]{easypage});

    try {
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        bean.setMapperLocations(resolver.getResources("classpath*:mapper/*Mapper.xml"));
    } catch (Exception e) {
        e.printStackTrace();
    }
    return bean.getObject();
}
``````

### 三、mapper中传入 PageParam分页参数，返回值为 Page，插件就会自动对其进行分页。

```
@Mapper
public interface UserMapper {
    Page<UserVo> getList(PageParam page) throws Exception;
}
```

```
@RequestMapping(value = "/getAll", method = RequestMethod.GET)
public PageResult getAll(PageParam page) throws Exception {
    Page<UserVo> list = mapper.getList(page);
    PageResult ret = new PageResult(list);
    return ret;
}
```

访问 http://127.0.0.1:8080/user/getAll?index=1&rows=10 ，得到分页结果如下：
![](page_result.png)


### 四、暂时停止分页
```
@Configuration
public class EasyPageConfig {

    @Bean
    public PageInterceptor pageHelper() {
        PageInterceptor easyPage = new PageInterceptor();
        Properties p = new Properties();
        p.setProperty("pageEnabled", "false");// 不分页
        easyPage.setProperties(p);
        return easyPage;
    }

}
```

## 常见问题

1、 分页没有生效

* PageParam 参数中 index 默认值为0，1 表示第一页，以此类推。
* PageParam 参数中 rows 默认值为 Integer.MAX_VALUE ，为默认值时不会分页。

2、 是否需要配置数据库类型
* 不需要配置，插件会根据数据库元数据自动识别数据库类型。