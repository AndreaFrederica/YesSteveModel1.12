package com.elfmcys.yesstevemodel.model.format;

import com.elfmcys.yesstevemodel.resource.models.MainModelInfo;
import com.elfmcys.yesstevemodel.resource.models.Metadata;
import com.elfmcys.yesstevemodel.resource.models.ModelProperties;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class ServerModelInfo {
    @SerializedName("textures")
    @Expose
    private final Set<String> textures;
    @SerializedName("need_auth")
    @Expose
    private final boolean needAuth;
    @Expose(serialize = false, deserialize = false)
    private final Type type;
    @Expose(serialize = false, deserialize = false)
    private String md5;

    @Expose(serialize = false, deserialize = false)
    private final Metadata metadata;
    @Expose(serialize = false, deserialize = false)
    private final ModelProperties modelProperties;
    @Expose(serialize = false, deserialize = false)
    private final MainModelInfo mainModelInfo;
    @Expose(serialize = false, deserialize = false)
    private final int formatVersion;
    @Expose(serialize = false, deserialize = false)
    private final String modelHash;
    @Expose(serialize = false, deserialize = false)
    private final String extra;
    @Expose(serialize = false, deserialize = false)
    private final long timestamp;
    @Expose(serialize = false, deserialize = false)
    private final String rand;

    public ServerModelInfo(Set<String> textures, boolean needAuth, Type type) {
        this.textures = textures;
        this.needAuth = needAuth;
        this.type = type;
        this.metadata = null;
        this.modelProperties = null;
        this.mainModelInfo = null;
        this.formatVersion = 65535;
        this.modelHash = "";
        this.extra = "";
        this.timestamp = 0L;
        this.rand = "";
    }

    public ServerModelInfo(Metadata metadata, ModelProperties modelProperties, MainModelInfo mainModelInfo, int formatVersion, String modelHash, String extra, long timestamp, String rand) {
        this(metadata, modelProperties, mainModelInfo, formatVersion, modelHash, extra, timestamp, rand,
                modelProperties != null && !modelProperties.isFree());
    }

    public ServerModelInfo(Metadata metadata, ModelProperties modelProperties, MainModelInfo mainModelInfo, int formatVersion, String modelHash, String extra, long timestamp, String rand, boolean needAuth) {
        this.textures = Collections.emptySet();
        this.needAuth = needAuth;
        this.type = Type.YSM;
        this.metadata = metadata;
        this.modelProperties = modelProperties;
        this.mainModelInfo = mainModelInfo;
        this.formatVersion = formatVersion;
        this.modelHash = modelHash == null ? "" : modelHash;
        this.extra = extra == null ? "" : extra;
        this.timestamp = timestamp;
        this.rand = rand == null ? "" : rand;
        this.md5 = this.modelHash;
    }

    public Type getType() {
        return this.type;
    }

    public Set<String> getTextures() {
        return this.textures;
    }

    public Optional<String> getTexture() {
        return this.textures.stream().findFirst();
    }

    public boolean isNeedAuth() {
        return this.needAuth;
    }

    public String getMd5() {
        return this.md5 != null ? this.md5 : this.modelHash;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public ServerModelInfo withNeedAuth(boolean needAuth) {
        ServerModelInfo copy = new ServerModelInfo(this.metadata, this.modelProperties, this.mainModelInfo,
                this.formatVersion, this.modelHash, this.extra, this.timestamp, this.rand, needAuth);
        copy.setMd5(this.md5);
        return copy;
    }

    public Metadata getExtraInfo() {
        return this.metadata;
    }

    public ModelProperties getModelProperties() {
        return this.modelProperties;
    }

    public MainModelInfo getMainModelInfo() {
        return this.mainModelInfo;
    }

    public int getFormatVersion() {
        return this.formatVersion;
    }

    public String getModelHash() {
        return this.modelHash;
    }

    public String getExtra() {
        return this.extra;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public String getRand() {
        return this.rand;
    }
}
