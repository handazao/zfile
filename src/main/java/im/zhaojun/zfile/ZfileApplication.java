package im.zhaojun.zfile;

import cn.xuyanwu.spring.file.storage.EnableFileStorage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


/**
 * @author zhaojun
 */
@EnableFileStorage
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true, proxyTargetClass = true)
@ServletComponentScan(basePackages = {"im.zhaojun.zfile.core.filter", "im.zhaojun.zfile.module.storage.filter"})
@ComponentScan(basePackages = "im.zhaojun.zfile.*")
public class ZfileApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZfileApplication.class, args);
    }

}
