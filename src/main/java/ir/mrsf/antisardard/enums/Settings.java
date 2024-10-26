package ir.mrsf.antisardard.enums;

import ir.mrsf.antisardard.utils.DataUtil;
import lombok.SneakyThrows;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.util.HashMap;

public enum Settings {

    IGNORE_MUTED("ignore-muted"),
    ADMIN_ID("admin-id"),
    TOKEN("token"),
    TARGETS("targets"),
    VOLUME_THRESHOLD("volume-threshold");

    private Object value;
    private final String path;

    Settings(String path) {
        this.path = path;
        this.reload();
    }

    @SuppressWarnings("unchecked")
    public <T> T getAs(Class<T> ignoredClazz) {
        return (T) this.value;
    }

    @SneakyThrows
    public void reload() {
        this.value = ((HashMap<?, ?>) ((HashMap<?, ?>) new Yaml().load(new FileInputStream(DataUtil.settingsFile)))
                .get("Settings")).get(path);
    }
}
