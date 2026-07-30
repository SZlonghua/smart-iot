package net.lab1024.sa.base.common.spi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


@Slf4j
public class SpringServiceContext implements ServiceContext {

    private final ApplicationContext applicationContext;

    public SpringServiceContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
    @Override
    public <T> Optional<T> getService(Class<T> service) {
        try {
            return Optional.of(applicationContext.getBean(service));
        } catch (Exception e) {
            log.error("load service [{}] error", service, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> getService(String service) {
        try {
            return Optional.of((T)applicationContext.getBean(service));
        } catch (Exception e) {
            log.error("load service [{}] error", service, e);
            return Optional.empty();
        }
    }

    @Override
    public <T> List<T> getServices(Class<T> service) {
        try {
            return new ArrayList<>(applicationContext.getBeansOfType(service).values());
        }catch (Exception e){
            log.error("load service [{}] error", service, e);
            return Collections.emptyList();
        }
    }

}
