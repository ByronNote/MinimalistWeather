package cn.byronlab.weather.data.mapper;

import cn.byronlab.weather.domain.model.City;

public final class LegacyCityDomainMapper {

    public City map(cn.byronlab.weather.data.db.entities.City source) {
        if (source == null) {
            return null;
        }
        return new City(
                String.valueOf(source.getCityId()),
                safeString(source.getCityName()),
                safeString(source.getCityNameEn()),
                safeString(source.getRoot()),
                safeString(source.getParent()),
                safeString(source.getLon()),
                safeString(source.getLat())
        );
    }

    public City map(cn.byronlab.weather.data.db.entities.minimalist.Weather source) {
        if (source == null) {
            return null;
        }
        return new City(
                safeString(source.getCityId()),
                safeString(source.getCityName()),
                safeString(source.getCityNameEn()),
                "",
                "",
                "",
                ""
        );
    }

    private String safeString(String value) {
        return value == null ? "" : value;
    }
}
