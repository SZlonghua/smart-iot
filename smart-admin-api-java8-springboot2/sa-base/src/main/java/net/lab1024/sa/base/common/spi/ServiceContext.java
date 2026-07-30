package net.lab1024.sa.base.common.spi;

import java.util.List;
import java.util.Optional;

public interface ServiceContext {

    <T> Optional<T> getService(Class<T> service);

    <T> Optional<T> getService(String service);

    <T> List<T> getServices(Class<T> service);

}
