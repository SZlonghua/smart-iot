package net.lab1024.sa.base.module.support.thingsmodel;

import net.lab1024.sa.base.metadata.EventMetadata;
import net.lab1024.sa.base.metadata.FunctionMetadata;
import net.lab1024.sa.base.metadata.PropertyMetadata;
import net.lab1024.sa.base.metadata.ThingsMetadata;
import net.lab1024.sa.base.module.support.json.IJsonNode;
import net.lab1024.sa.base.module.support.json.JsonUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 物模型元数据 Iot 实现 —— 懒解析聚合根
 *
 * @Author 廖涛
 * @Date 2026/06/10
 * @Copyright 1024创新实验室
 */
public class IotThingsMetadata implements ThingsMetadata {

    private IJsonNode root;
    private DataTypeCodecs codecs;

    private List<IotPropertyMetadata> properties;
    private List<IotFunctionMetadata> functions;
    private List<IotEventMetadata> events;

    IotThingsMetadata(IJsonNode root, DataTypeCodecs codecs) {
        this.root = root;
        this.codecs = codecs;
        this.properties = buildProperties();
        this.functions = buildFunctions();
        this.events = buildEvents();
    }

    IJsonNode root() { return root; }

    @Override
    public String toJson() { return JsonUtil.toJson(root); }

    @Override
    public void fromJson(String json) {
        this.root = JsonUtil.parse(json);
        if (this.codecs == null) this.codecs = new DataTypeCodecs();
        this.properties = buildProperties();
        this.functions = buildFunctions();
        this.events = buildEvents();
    }

    @Override public List<IotPropertyMetadata> getProperties() { return properties; }
    @Override public List<IotFunctionMetadata> getFunctions() { return functions; }
    @Override public List<IotEventMetadata> getEvents() { return events; }

    @Override
    public IotPropertyMetadata getPropertyOrNull(String id) {
        for (IotPropertyMetadata p : properties) {
            if (id.equals(p.getId())) return p;
        }
        return null;
    }

    @Override
    public IotFunctionMetadata getFunctionOrNull(String id) {
        for (IotFunctionMetadata f : functions) {
            if (id.equals(f.getId())) return f;
        }
        return null;
    }

    @Override
    public IotEventMetadata getEventOrNull(String id) {
        for (IotEventMetadata e : events) {
            if (id.equals(e.getId())) return e;
        }
        return null;
    }

    private List<IotPropertyMetadata> buildProperties() {
        List<IotPropertyMetadata> list = new ArrayList<IotPropertyMetadata>();
        IJsonNode arr = root.get("properties");
        if (arr != null && arr.isArray()) {
            for (int i = 0; i < arr.size(); i++) {
                IJsonNode pn = arr.get(i);
                if (pn != null) list.add(new IotPropertyMetadata(pn, codecs));
            }
        }
        return list;
    }

    private List<IotFunctionMetadata> buildFunctions() {
        List<IotFunctionMetadata> list = new ArrayList<IotFunctionMetadata>();
        IJsonNode arr = root.get("functions");
        if (arr != null && arr.isArray()) {
            for (int i = 0; i < arr.size(); i++) {
                IJsonNode fn = arr.get(i);
                if (fn != null) list.add(new IotFunctionMetadata(fn, codecs));
            }
        }
        return list;
    }

    private List<IotEventMetadata> buildEvents() {
        List<IotEventMetadata> list = new ArrayList<IotEventMetadata>();
        IJsonNode arr = root.get("events");
        if (arr != null && arr.isArray()) {
            for (int i = 0; i < arr.size(); i++) {
                IJsonNode en = arr.get(i);
                if (en != null) list.add(new IotEventMetadata(en, codecs));
            }
        }
        return list;
    }

    @Override
    public List<String> validate() {
        List<String> errors = new ArrayList<String>();

        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < properties.size(); i++) {
            PropertyMetadata prop = properties.get(i);
            if (!ids.add(prop.getId())) {
                errors.add("properties[" + i + "]: id重复: " + prop.getId());
            }
            errors.addAll(prop.validate());
        }

        ids.clear();
        for (int i = 0; i < functions.size(); i++) {
            FunctionMetadata func = functions.get(i);
            if (!ids.add(func.getId())) {
                errors.add("functions[" + i + "]: id重复: " + func.getId());
            }
            errors.addAll(func.validate());
        }

        ids.clear();
        for (int i = 0; i < events.size(); i++) {
            EventMetadata event = events.get(i);
            if (!ids.add(event.getId())) {
                errors.add("events[" + i + "]: id重复: " + event.getId());
            }
            errors.addAll(event.validate());
        }

        return errors;
    }
}
