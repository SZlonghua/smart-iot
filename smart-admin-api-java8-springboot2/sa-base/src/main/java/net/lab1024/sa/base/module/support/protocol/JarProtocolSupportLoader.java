package net.lab1024.sa.base.module.support.protocol;

import lombok.extern.slf4j.Slf4j;
import net.lab1024.sa.base.common.protocol.ProtocolSupport;
import net.lab1024.sa.base.common.protocol.ProtocolSupportDefinition;
import net.lab1024.sa.base.common.protocol.ProtocolSupportLoader;
import net.lab1024.sa.base.common.spi.ProtocolSupportProvider;
import net.lab1024.sa.base.common.spi.ServiceContext;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import reactor.core.publisher.Mono;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Jar 协议加载器 — 从外部 jar 包加载 ProtocolSupportProvider 实现。
 *
 * @Author 廖涛
 * @Date 2026/07/27
 * @Copyright 1024创新实验室
 */
@Slf4j
public class JarProtocolSupportLoader implements ProtocolSupportLoader {

    private static final String SCAN_PACKAGE = "net.lab1024.sa";
    private final Map<String, LoaderEntry> loaderCache = new ConcurrentHashMap<>();
    private final ServiceContext serviceContext;

    public JarProtocolSupportLoader(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return "jar".equals(definition.getLoader());
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        return Mono.fromCallable(() -> {
            log.info("[JarLoader] 加载协议 — id={}, jarPath={}", definition.getId(), definition.getJarPath());
            URLClassLoader classLoader = createClassLoader(definition);
            ProtocolSupportProvider provider = scanAndInstantiate(classLoader, definition.getId());
            loaderCache.put(definition.getId(), new LoaderEntry(definition.getId(), classLoader, provider));
            return provider.create(serviceContext);
        }).flatMap(support -> support);
    }

    private URLClassLoader createClassLoader(ProtocolSupportDefinition definition) throws Exception {
        LoaderEntry old = loaderCache.get(definition.getId());
        if (old != null) {
            closeEntry(old);
        }
        URL url = toURL(definition.getJarPath());
        return new URLClassLoader(new URL[]{url}, JarProtocolSupportLoader.class.getClassLoader());
    }

    private URL toURL(String jarPath) throws Exception {
        if (jarPath.contains("://")) {
            return new URL(jarPath);
        }
        return Paths.get(jarPath).toUri().toURL();
    }

    @Override
    public void close(String id) {
        LoaderEntry entry = loaderCache.remove(id);
        if (entry != null) {
            closeEntry(entry);
        }
    }

    private void closeEntry(LoaderEntry entry) {
        log.info("[JarLoader] 关闭协议 — id={}", entry.id);
        try {
            entry.provider.dispose();
        } catch (Exception e) {
            log.warn("[JarLoader] provider.dispose 失败", e);
        }
        try {
            entry.classLoader.close();
        } catch (Exception e) {
            log.warn("[JarLoader] 关闭 ClassLoader 失败", e);
        }
    }

    private ProtocolSupportProvider scanAndInstantiate(URLClassLoader classLoader, String id) throws Exception {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .addClassLoaders(classLoader)
                .setUrls(classLoader.getURLs())
                .filterInputsBy(new FilterBuilder().includePackage(SCAN_PACKAGE))
                .addScanners(Scanners.SubTypes));
        Set<Class<? extends ProtocolSupportProvider>> subTypes = reflections.getSubTypesOf(ProtocolSupportProvider.class);
        for (Class<? extends ProtocolSupportProvider> clazz : subTypes) {
            log.info("[JarLoader] 加载成功 — id={}, class={}", id, clazz.getName());
            return clazz.newInstance();
        }
        throw new IllegalStateException("jar 中未找到 ProtocolSupportProvider 实现: " + id);
    }

    private static class LoaderEntry {
        final String id;
        final URLClassLoader classLoader;
        final ProtocolSupportProvider provider;

        LoaderEntry(String id, URLClassLoader classLoader, ProtocolSupportProvider provider) {
            this.id = id;
            this.classLoader = classLoader;
            this.provider = provider;
        }
    }
}
