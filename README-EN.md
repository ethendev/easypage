## easypage
A very simple mybatis pagination plugin, supports MySQL, Oracle.

## Tutorial

### 1. Adding dependencies in pom.xml

```
<dependency>
   <groupId>com.github.ethendev</groupId>
   <artifactId>easypage</artifactId>
   <version>1.0.1</version>
 </dependency>
```

### 2. Configure plugins
Non-SpringBoot project, add the following code in mybatis-config.xml:
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

If it is a SpringBoot project, add the plugin to the SqlSessionFactory configuration
````
@Bean(name = "sqlSessionFactory")
public SqlSessionFactory sqlSessionFactoryBean() throws Exception {
    SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
    bean.setDataSource(dataSource);

    //add plugin
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

### 3. Add pageParam parameter in the mapper. The plugin will automatically page it.


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

Visit http://127.0.0.1:8080/user/getAll?index=1&rows=10 to get the result of paging:
![](page_result.png)

### 4. Temporarily stop paging
```
@Configuration
public class EasyPageConfig {

    @Bean
    public PageInterceptor pageHelper() {
        PageInterceptor easyPage = new PageInterceptor();
        Properties p = new Properties();
        p.setProperty("pageEnabled", "false");
        easyPage.setProperties(p);
        return easyPage;
    }

}
```

## FAQ

1、 Configuration is correct, but no paging
* In the PageParam parameter, the index default value is 0, and rows default value is Integer.MAX_VALUE. As long as the rows is the default value, it will not paged.
* Index is counted from 1。

2、 Whether you need to configure the database type?
* You do not need to configure it, the plugin will automatically recognize the database type based on the database metadata.