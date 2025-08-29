package org.manusmith.shell.service;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.manusmith.shell.dto.AuthorMeta;

public class SharedDataService {

    private static final SharedDataService INSTANCE = new SharedDataService();

    private final ObjectProperty<AuthorMeta> authorMetaProperty = new SimpleObjectProperty<>();

    private SharedDataService() {
    }

    public static SharedDataService getInstance() {
        return INSTANCE;
    }

    public ObjectProperty<AuthorMeta> authorMetaProperty() {
        return authorMetaProperty;
    }

    public AuthorMeta getAuthorMeta() {
        return authorMetaProperty.get();
    }

    public void setAuthorMeta(AuthorMeta authorMeta) {
        this.authorMetaProperty.set(authorMeta);
    }
}
