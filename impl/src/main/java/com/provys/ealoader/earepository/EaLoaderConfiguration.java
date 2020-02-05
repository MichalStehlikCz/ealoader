package com.provys.ealoader.earepository;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nonnull;
import java.util.Objects;

@ConfigurationProperties(prefix = "ea")
class EaLoaderConfiguration {
    @Nonnull
    private final String address;
    @Nonnull
    private final String user;
    @Nonnull
    private final String pwd;

    @ConstructorBinding
    EaLoaderConfiguration(String address, @DefaultValue("") String user, @DefaultValue("") String pwd) {
        if (Objects.requireNonNull(address, "Property ea.address not specified").isBlank()) {
            throw new IllegalArgumentException("Property ea.address cannot be blank");
        }
        this.address = address;
        this.user = Objects.requireNonNull(user);
        this.pwd = Objects.requireNonNull(pwd);
    }

    @Nonnull
    public String getAddress() {
        return address;
    }

    @Nonnull
    public String getUser() {
        return user;
    }

    @Nonnull
    public String getPwd() {
        return pwd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EaLoaderConfiguration)) return false;
        EaLoaderConfiguration that = (EaLoaderConfiguration) o;
        return getAddress().equals(that.getAddress()) &&
                getUser().equals(that.getUser()) &&
                getPwd().equals(that.getPwd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getUser(), getPwd());
    }

    @Override
    public String toString() {
        return "EaLoaderConfiguration{" +
                "address='" + address + '\'' +
                ", user='" + user + '\'' +
                '}';
    }
}
