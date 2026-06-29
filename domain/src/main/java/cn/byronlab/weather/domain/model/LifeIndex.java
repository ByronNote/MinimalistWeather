package cn.byronlab.weather.domain.model;

import java.util.Objects;

public final class LifeIndex {

    private final String cityId;
    private final String name;
    private final String level;
    private final String details;

    public LifeIndex(String cityId, String name, String level, String details) {
        this.cityId = cityId;
        this.name = name;
        this.level = level;
        this.details = details;
    }

    public String getCityId() {
        return cityId;
    }

    public String getName() {
        return name;
    }

    public String getLevel() {
        return level;
    }

    public String getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LifeIndex)) {
            return false;
        }
        LifeIndex lifeIndex = (LifeIndex) o;
        return Objects.equals(cityId, lifeIndex.cityId)
                && Objects.equals(name, lifeIndex.name)
                && Objects.equals(level, lifeIndex.level)
                && Objects.equals(details, lifeIndex.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cityId, name, level, details);
    }
}
